import net.hollowcube.polar.*;
import java.nio.file.*;
class VerifyVoidTemplate {
 public static void main(String[] args)throws Exception {
  net.minestom.server.MinecraftServer.init();
  var method=Class.forName("dev.daweapon.type.skyblockgeneric.user.island.IslandWorldStorage").getDeclaredMethod("templateWorld");method.setAccessible(true);
  var w=(PolarWorld)method.invoke(null);
  if(w.chunks().size()!=6 || w.chunkAt(9,9)!=null || w.chunkAt(0,0)==null || w.chunkAt(0,2)==null)throw new AssertionError("Invalid template bounds");
  var original=PolarReader.read(Files.readAllBytes(Path.of("configuration/world/hypixel_skyblock_island_template.polar")));
  for(var c:w.chunks()){
   var before=new PolarWorld(original.version(),original.dataVersion(),original.compression(),original.minSection(),original.maxSection(),original.userData(),java.util.List.of(original.chunkAt(c.x(),c.z())));
   var after=new PolarWorld(w.version(),w.dataVersion(),w.compression(),w.minSection(),w.maxSection(),w.userData(),java.util.List.of(c));
   if(!java.util.Arrays.equals(PolarWriter.write(before),PolarWriter.write(after)))throw new AssertionError("Starter chunk altered");
  }
  System.out.println("PASS: actual template loader retains all starter chunk data exactly and excludes surrounding terrain.");
 }
}
