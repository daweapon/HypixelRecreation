package dev.daweapon.type.skyblockgeneric.starlyn;

import lombok.Getter;

@Getter
public enum StarlynResource {
    LUSHLILAC(StarlynSister.AGATHA),
    SEA_LUMIES(StarlynSister.AGATHA),
    HONEYCOMB(StarlynSister.MIRIA),
    RUBY_VEILSHROOM(StarlynSister.MIRIA);

    private final StarlynSister sister;

    StarlynResource(StarlynSister sister) {
        this.sister = sister;
    }
}
