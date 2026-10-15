package dev.daweapon.type.skyblockgeneric.data;

import dev.daweapon.LinkedField;
import dev.daweapon.commons.data.SharedData;
import dev.daweapon.commons.protocol.Serializer;
import dev.daweapon.commons.skyblock.CoopLinks;
import dev.daweapon.lock.LockAcquisitionException;
import org.tinylog.Logger;

import java.util.UUID;
import java.util.function.Function;

// Performs coop-safe read-modify-write updates to a shared, coop-linked datapoint field using
// a distributed transaction, reporting whether the change applied, was unchanged, or failed.
public final class DataMutexService {

    public enum Outcome {
        APPLIED,
        UNCHANGED,
        UNLINKED,
        BUSY,
        FAILED;

        public boolean isFailure() {
            return this == UNLINKED || this == BUSY || this == FAILED;
        }
    }

    private DataMutexService() {}

    @SuppressWarnings("unchecked")
    public static <T> Outcome withSynchronizedData(UUID profileId, SkyBlockDataHandler.Data dataType,
                                                   Function<T, T> operation) {
        LinkedField<UUID, String> field = dataType.coopField();
        if (field == null) return Outcome.UNLINKED;

        Serializer<T> serializer = (Serializer<T>) dataType.getDefaultDatapoint().getSerializer();
        return withSynchronizedField(coopIdFor(profileId), field, stored -> {
            T current = stored != null
                    ? serializer.deserialize(stored)
                    : (T) dataType.getDefaultDatapoint().deepClone().getValue();
            T modified = operation.apply(current);
            return modified == null ? null : serializer.serialize(modified);
        });
    }

    public static <T> Outcome withSynchronizedField(UUID coopId, LinkedField<UUID, T> field,
                                                    Function<T, T> operation) {
        if (coopId == null || field == null) return Outcome.UNLINKED;

        try {
            Boolean applied = SharedData.profile().transactionDirect(coopId, CoopLinks.COOP, tx -> {
                T modified = operation.apply(tx.get(field));
                if (modified == null) return Boolean.FALSE;
                tx.set(field, modified);
                return Boolean.TRUE;
            });
            return Boolean.TRUE.equals(applied) ? Outcome.APPLIED : Outcome.UNCHANGED;
        } catch (LockAcquisitionException e) {
            return Outcome.BUSY;
        } catch (Exception e) {
            Logger.error(e, "Failed synchronized coop write of {} on coop {}", field.fullKey(), coopId);
            return Outcome.FAILED;
        }
    }

    public static UUID coopIdFor(UUID profileId) {
        if (profileId == null) return null;
        return SharedData.profile().getLinkKey(profileId, CoopLinks.COOP).orElse(profileId);
    }
}
