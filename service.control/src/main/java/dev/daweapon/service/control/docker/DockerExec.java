package dev.daweapon.service.control.docker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * Runs a command inside a running container: the Engine API equivalent of `docker exec`.
 *
 * Used only for read-only probes (hashing files in place), so the output is collected whole
 * rather than streamed. The start call carries an explicit budget because Docker withholds the
 * response until the command exits.
 */
public final class DockerExec {
    private final DockerSocketClient client;

    public DockerExec(DockerSocketClient client) {
        this.client = client;
    }

    public record Result(int exitCode, String output) {
        public boolean ok() {
            return exitCode == 0;
        }
    }

    public Result run(String container, List<String> command, Duration budget) {
        JsonObject create = new JsonObject();
        create.addProperty("AttachStdout", true);
        create.addProperty("AttachStderr", true);
        create.addProperty("Tty", false);
        JsonArray cmd = new JsonArray();
        command.forEach(cmd::add);
        create.add("Cmd", cmd);

        DockerResponse created = client.request("POST",
                "/containers/" + URLEncoder.encode(container, StandardCharsets.UTF_8) + "/exec",
                create.toString());
        if (!created.isSuccess()) {
            throw new DockerException("Creating exec in " + container + " failed with HTTP "
                    + created.status() + ": " + created.body());
        }
        String execId = JsonParser.parseString(created.body()).getAsJsonObject().get("Id").getAsString();

        DockerResponse started = client.post("/exec/" + execId + "/start",
                "{\"Detach\":false,\"Tty\":false}", budget);
        if (!started.isSuccess()) {
            throw new DockerException("Running exec in " + container + " failed with HTTP "
                    + started.status() + ": " + started.body());
        }
        String output = LogFrameDecoder.decode(started.bodyBytes());

        int exitCode = -1;
        DockerResponse inspected = client.get("/exec/" + execId + "/json");
        if (inspected.isSuccess()) {
            JsonObject root = JsonParser.parseString(inspected.body()).getAsJsonObject();
            if (root.has("ExitCode") && !root.get("ExitCode").isJsonNull()) {
                exitCode = root.get("ExitCode").getAsInt();
            }
        }
        return new Result(exitCode, output);
    }
}
