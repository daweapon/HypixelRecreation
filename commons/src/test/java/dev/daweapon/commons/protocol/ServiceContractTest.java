package dev.daweapon.commons.protocol;

import dev.daweapon.commons.ServiceType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceContractTest {
    private static final Path ROOT = Path.of("..").toAbsolutePath().normalize();
    private static final Path PROTOCOL_ROOT = Path.of("commons", "src", "main", "java", "dev", "daweapon", "commons", "protocol", "objects");
    private static final Set<String> NOT_SKYBLOCK_SERVICES = Set.of("service.generic", "service.control");
    private static final Set<String> MESSAGE_SHAPE_ONLY = Set.of("DarkAuctionEventProtocol");
    private static final Set<String> EXPLICITLY_REGISTERED_HANDLERS = Set.of("PingEndpoint");
    private static final Map<String, ServiceType> DOMAIN_OWNERS = Map.ofEntries(
            Map.entry("api", ServiceType.API),
            Map.entry("auctions", ServiceType.AUCTION_HOUSE),
            Map.entry("bazaar", ServiceType.BAZAAR),
            Map.entry("darkauction", ServiceType.DARK_AUCTION),
            Map.entry("election", ServiceType.ELECTION),
            Map.entry("friend", ServiceType.FRIEND),
            Map.entry("guild", ServiceType.GUILD),
            Map.entry("itemtracker", ServiceType.ITEM_TRACKER),
            Map.entry("orchestrator", ServiceType.ORCHESTRATOR),
            Map.entry("party", ServiceType.PARTY),
            Map.entry("presence", ServiceType.FRIEND),
            Map.entry("punishment", ServiceType.PUNISHMENT),
            Map.entry("replay", ServiceType.REPLAY));

    private static final Pattern HANDLER = Pattern.compile("class\\s+(\\w+)[^{]*implements[^{]*RedisMessageHandler\\s*<");
    private static final Pattern HANDLER_PROTOCOL = Pattern.compile("protocol\\(\\)\\s*\\{\\s*return new ([\\w.]+)\\(|PROTOCOL\\s*=\\s*new ([\\w.]+)\\(");
    private static final Pattern PACKAGE = Pattern.compile("^package ([\\w.]+);", Pattern.MULTILINE);
    private static final Pattern PROTOCOL_CLASS = Pattern.compile("class\\s+(\\w+)\\s+extends\\s+RedisProtocol");
    private static final Pattern SCANNED_PACKAGE = Pattern.compile("(?:loopThroughPackage|instantiateSubtypes)\\(\\s*\"([\\w.]+)\"\\s*,\\s*RedisMessageHandler\\.class");
    private static final Pattern SERVICE_TYPE_USE = Pattern.compile("new ProxyService\\(ServiceType\\.(\\w+)\\)|requestService\\(ServiceType\\.(\\w+)");

    private record Source(String module, Path path, String text) {
    }

    private record Handler(String module, String className, String packageName, String protocol, String text) {
    }

    private record ServiceModule(String module, ServiceType type, String endpointsPackage) {
    }

    private static List<Source> sources;

    private static synchronized List<Source> sources() throws IOException {
        if (sources == null) {
            assertTrue(Files.exists(ROOT.resolve("settings.gradle.kts")), "tests must run from a module directory of the repository: " + ROOT);
            List<Source> found = new ArrayList<>();
            try (Stream<Path> modules = Files.list(ROOT)) {
                for (Path module : modules.filter(Files::isDirectory).toList()) {
                    Path main = module.resolve("src/main/java");
                    if (!Files.isDirectory(main)) continue;
                    try (Stream<Path> files = Files.walk(main)) {
                        for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                            found.add(new Source(module.getFileName().toString(), ROOT.relativize(file), Files.readString(file)));
                        }
                    }
                }
            }
            sources = found;
        }
        return sources;
    }

    private static Map<String, String> protocolDomains() throws IOException {
        Map<String, String> domains = new TreeMap<>();
        for (Source source : sources()) {
            String path = source.path().toString().replace('\\', '/');
            String marker = "/protocol/objects/";
            int index = path.indexOf(marker);
            if (!source.module().equals("commons") || index < 0) continue;
            String rest = path.substring(index + marker.length());
            String domain = rest.contains("/") ? rest.substring(0, rest.indexOf('/')) : "";
            Matcher matcher = PROTOCOL_CLASS.matcher(source.text());
            while (matcher.find()) domains.put(matcher.group(1), domain);
        }
        return domains;
    }

    private static List<Handler> handlers() throws IOException {
        List<Handler> handlers = new ArrayList<>();
        for (Source source : sources()) {
            Matcher declaration = HANDLER.matcher(source.text());
            if (!declaration.find()) continue;
            Matcher protocol = HANDLER_PROTOCOL.matcher(source.text());
            assertTrue(protocol.find(), source.path() + " implements RedisMessageHandler but its protocol() is not 'return new X()' or a PROTOCOL constant");
            String name = protocol.group(1) != null ? protocol.group(1) : protocol.group(2);
            Matcher pkg = PACKAGE.matcher(source.text());
            assertTrue(pkg.find(), source.path().toString());
            handlers.add(new Handler(source.module(), declaration.group(1), pkg.group(1), name.substring(name.lastIndexOf('.') + 1), source.text()));
        }
        return handlers;
    }

    private static List<ServiceModule> serviceModules() throws IOException {
        List<ServiceModule> modules = new ArrayList<>();
        Pattern type = Pattern.compile("getType\\(\\)\\s*\\{\\s*return ServiceType\\.(\\w+);");
        Pattern endpoints = Pattern.compile("loopThroughPackage\\(\\s*\"([\\w.]+)\"\\s*,\\s*RedisMessageHandler\\.class");
        for (Source source : sources()) {
            if (!source.module().startsWith("service.") || NOT_SKYBLOCK_SERVICES.contains(source.module())) continue;
            if (!source.text().contains("implements SkyBlockService")) continue;
            Matcher typeMatcher = type.matcher(source.text());
            assertTrue(typeMatcher.find(), source.path() + " has no 'return ServiceType.X' in getType()");
            Matcher endpointMatcher = endpoints.matcher(source.text());
            modules.add(new ServiceModule(source.module(), ServiceType.valueOf(typeMatcher.group(1)), endpointMatcher.find() ? endpointMatcher.group(1) : null));
        }
        return modules;
    }

    private static Map<String, ServiceType> protocolOwners() throws IOException {
        Map<String, ServiceType> modulesByName = new HashMap<>();
        for (ServiceModule module : serviceModules()) modulesByName.put(module.module(), module.type());
        Map<String, ServiceType> owners = new TreeMap<>();
        for (Handler handler : handlers()) {
            ServiceType type = modulesByName.get(handler.module());
            if (type != null) owners.put(handler.protocol(), type);
        }
        return owners;
    }

    @Test
    void everyServiceTypeHasExactlyOneServiceModuleAndBack() throws IOException {
        Map<ServiceType, List<String>> byType = new EnumMap<>(ServiceType.class);
        for (ServiceModule module : serviceModules()) byType.computeIfAbsent(module.type(), ignored -> new ArrayList<>()).add(module.module());

        for (ServiceType type : ServiceType.values()) {
            assertEquals(1, byType.getOrDefault(type, List.of()).size(), type + " needs exactly one service module but has " + byType.get(type));
        }
        assertEquals(ServiceType.values().length, serviceModules().size());
    }

    @Test
    void everyServiceModuleIsIncludedInTheBuild() throws IOException {
        String settings = Files.readString(ROOT.resolve("settings.gradle.kts"));
        for (ServiceModule module : serviceModules()) {
            assertTrue(settings.contains("include(\":" + module.module() + "\")"), module.module() + " is not in settings.gradle.kts");
        }
    }

    @Test
    void everyHandlerHasAProtocolThatExists() throws IOException {
        Set<String> known = protocolDomains().keySet();
        for (Handler handler : handlers()) {
            assertTrue(known.contains(handler.protocol()), handler.module() + "/" + handler.className() + " serves unknown protocol " + handler.protocol());
        }
        assertTrue(handlers().size() >= 90, "expected the full handler set but found " + handlers().size());
    }

    @Test
    void everyProtocolHasAHandlerOrIsDocumentedAsMessageShapeOnly() throws IOException {
        Set<String> handled = handlers().stream().map(Handler::protocol).collect(Collectors.toSet());
        Set<String> unhandled = new TreeSet<>(protocolDomains().keySet());
        unhandled.removeAll(handled);
        unhandled.removeAll(MESSAGE_SHAPE_ONLY);
        assertEquals(Set.of(), unhandled, "protocols nobody answers");
        for (String shapeOnly : MESSAGE_SHAPE_ONLY) {
            assertTrue(protocolDomains().containsKey(shapeOnly), shapeOnly + " no longer exists, drop it from the allow-list");
            assertTrue(!handled.contains(shapeOnly), shapeOnly + " now has a handler, drop it from the allow-list");
        }
    }

    @Test
    void noProtocolIsServedByTwoServices() throws IOException {
        Map<String, Set<String>> modulesByProtocol = new TreeMap<>();
        Set<String> serviceModules = serviceModules().stream().map(ServiceModule::module).collect(Collectors.toSet());
        serviceModules.add("service.generic");
        for (Handler handler : handlers()) {
            if (serviceModules.contains(handler.module())) {
                modulesByProtocol.computeIfAbsent(handler.protocol(), ignored -> new TreeSet<>()).add(handler.module());
            }
        }
        modulesByProtocol.remove("PingProtocol");
        Map<String, Set<String>> shared = modulesByProtocol.entrySet().stream().filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, TreeMap::new));
        assertEquals(Map.of(), shared, "protocols with more than one answering service");
    }

    @Test
    void serviceHandledProtocolsLiveInTheirOwnServicesDomain() throws IOException {
        Map<String, String> domains = protocolDomains();
        List<String> misplaced = new ArrayList<>();
        for (Map.Entry<String, ServiceType> owner : protocolOwners().entrySet()) {
            if (owner.getKey().equals("PingProtocol")) continue;
            String domain = domains.get(owner.getKey());
            ServiceType expected = DOMAIN_OWNERS.get(domain);
            if (expected == null) {
                misplaced.add(owner.getKey() + " is served by " + owner.getValue() + " but domain '" + domain + "' has no owner in this test");
            } else if (expected != owner.getValue()) {
                misplaced.add(owner.getKey() + " is in domain '" + domain + "' (" + expected + ") but served by " + owner.getValue());
            }
        }
        assertEquals(List.of(), misplaced);
    }

    @Test
    void everyServiceScansTheEndpointPackageHoldingItsHandlers() throws IOException {
        Map<String, Set<String>> packagesByModule = new HashMap<>();
        for (Handler handler : handlers()) {
            packagesByModule.computeIfAbsent(handler.module(), ignored -> new TreeSet<>()).add(handler.packageName());
        }
        for (ServiceModule module : serviceModules()) {
            Set<String> packages = packagesByModule.getOrDefault(module.module(), Set.of());
            if (module.endpointsPackage() == null) {
                assertEquals(Set.of(), packages, module.module() + " has handlers but does not scan any package for them");
                continue;
            }
            for (String handlerPackage : packages) {
                assertTrue(handlerPackage.equals(module.endpointsPackage()) || handlerPackage.startsWith(module.endpointsPackage() + "."),
                        module.module() + " handler package " + handlerPackage + " is not under the scanned " + module.endpointsPackage());
            }
        }
    }

    @Test
    void everyHandlerIsRegisteredByAScannedPackageOrCreatedByItsLoader() throws IOException {
        Set<String> scanned = new TreeSet<>();
        for (Source source : sources()) {
            Matcher matcher = SCANNED_PACKAGE.matcher(source.text());
            while (matcher.find()) scanned.add(matcher.group(1));
        }
        assertTrue(scanned.size() >= 25, "expected the scanned handler packages but found " + scanned);

        List<String> orphans = new ArrayList<>();
        for (Handler handler : handlers()) {
            boolean scannedPackage = scanned.stream().anyMatch(p -> handler.packageName().equals(p) || handler.packageName().startsWith(p + "."));
            if (scannedPackage || EXPLICITLY_REGISTERED_HANDLERS.contains(handler.className())) continue;
            Pattern created = Pattern.compile("new (?:[\\w.]+\\.)?" + handler.className() + "\\(");
            boolean createdElsewhere = sources().stream()
                    .anyMatch(source -> source.module().equals(handler.module()) && created.matcher(source.text()).find());
            if (!createdElsewhere) orphans.add(handler.module() + "/" + handler.className());
        }
        assertEquals(List.of(), orphans, "handlers that nothing registers");
    }

    @Test
    void clientsCallTheServiceThatAnswersTheProtocol() throws IOException {
        Map<String, ServiceType> owners = protocolOwners();
        owners.remove("PingProtocol");
        List<String> wrong = new ArrayList<>();
        int checked = 0;
        for (Source source : sources()) {
            if (source.module().equals("commons") || source.module().startsWith("service.")) continue;
            Set<String> targets = new HashSet<>();
            Matcher matcher = SERVICE_TYPE_USE.matcher(source.text());
            while (matcher.find()) targets.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            if (targets.size() != 1) continue;
            ServiceType target = ServiceType.valueOf(targets.iterator().next());
            for (Map.Entry<String, ServiceType> owner : owners.entrySet()) {
                if (!Pattern.compile("\\b" + owner.getKey() + "\\b").matcher(source.text()).find()) continue;
                checked++;
                if (owner.getValue() != target) {
                    wrong.add(source.path() + " calls " + target + " but " + owner.getKey() + " is answered by " + owner.getValue());
                }
            }
        }
        assertTrue(checked >= 50, "expected to check most client call sites but checked " + checked);
        assertEquals(List.of(), wrong);
    }
}
