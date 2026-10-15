package dev.daweapon.dungeons;

// Manual test/demo entry point that generates a sample dungeon and prints its layout and generation time.
public class DungeonsTest {
    static void main(String[] args) {
        DungeonsData data = new DungeonsData(5, 5)
                .with(DungeonRoomType.FAIRY, new DungeonsData.RoomData(1, 1))
                .with(DungeonRoomType.PUZZLE, new DungeonsData.RoomData(2, 4))
                .with(DungeonRoomType.MINI_BOSS, new DungeonsData.RoomData(1, 1));

        GeneratorService generatorService = DungeonsAPI.getGeneratorService(data);
        CatacombsDungeon dungeon = generatorService.generate().join();

        System.out.println("Generated dungeon: \n" + dungeon);
        System.out.println(System.currentTimeMillis() - generatorService.getGenerationStartTime());
    }
}
