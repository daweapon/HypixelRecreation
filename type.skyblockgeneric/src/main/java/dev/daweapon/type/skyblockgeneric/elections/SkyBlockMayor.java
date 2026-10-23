package dev.daweapon.type.skyblockgeneric.elections;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

// The pool of mayoral candidates (regular, special and one-time), each with its display skin and the set
// of perks it can grant when elected. Perk text follows the wiki's Module:Mayor/Data. Barry, Scorpius,
// Derpy and Jerry carry Hypixel's real skins without a signature, which no public source holds.
@Getter
public enum SkyBlockMayor {
    AATROX(
        "Aatrox", Kind.REGULAR,
        "ewogICJ0aW1lc3RhbXAiIDogMTU5Nzc4MTIyNjczMywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzFiZGY1MDViYjhjMGYxZjMzNjVhMDMwMzJkZTE5MzE2NjNmZjcxYzU3ZTAyMjU1OGRlMzEyYjhmMWI1YzQ0NSIKICAgIH0KICB9Cn0=",
        "fuSuJuBDgFDBV9878jr/Sgvg5w+DJ1YaXBmgAPurDoScZtarz1pwQ0Tsw+kBPzNYBhD2hOaGHna32ttqlKKufdXFJG09uPlv8nSCHNA7rwl74L0KO7wOn4Bxsa6cS0UsGj3AJYzJ0tnTcF3ILpHUF+C2aYgRvTjm0UXWo1Ai0+bjN67ecb3MlcsuCBDpTQQl8ujvW3xp2krxyJeeKeERMAGTW0QgsuJCrkN3iXrIiv4VWn1TBslutuu8vsltkYcRZcsUljWNtCHYQ/eBt6B8qdoEhierPCepdpdJzwYmcWMaxLWHO336g2Sp541hJDVtS5yCN48rY6UREJfcISJS0HjmVo0862h0JMKNT6ceIAbF/CzLkL+uAnaOCUNdPedrRjevWrKAXAGfd9JXK073ge3X/qDr0HahNFManGA030qHSTz3C7rqTrCVPOLg7QSipFZH0HKGrEyhCPnpZ10zC4UHeFL9zwCEi5L7O+qIrPraJmdJ2lU7QA33mLstL6AEabLZ2LluRRpki+9GIJ/gfjlB50fIHYOJPTykZymPIp1R6fAXyNn32m15WP38c5c4akYTfTq+1+zBupfxYOD2BZTd3x2Ce2klEMnn4Wo4CwkENI/37/kaure7CB0TpM9VCMJaNQZbIc10ehr93qJGCROSxpvT/+KqN6nRhfs7hVk=",
        new Perk[]{Perk.SLASHED_PRICING, Perk.SLAYER_XP_BUFF, Perk.PATHFINDER}
    ),
    COLE(
        "Cole", Kind.REGULAR,
        "ewogICJ0aW1lc3RhbXAiIDogMTU5Nzc4MDkwNjkxMiwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY0MjJkZTA4ODQ4OTUyZDFjYmVhZDY2YmJiYWQ2ZjA3MTkxYmRjYzk1MmYzZDEwMzZhZWIwYzIyOTM4ZjM5YiIKICAgIH0KICB9Cn0=",
        "FWvg7q7AORwUPOYVUNH4xjgwxOhzLm2rnBJH4mHkVA/BTYnaLD/hkJkUeMxYFgjArbrw8EvjUmhix7UgptWx0mHWmqssceBuXFHlq1JkzpPubfXpGRpvT+2AUqGSgHGHW9Gxeo0jtGr+mOvIABP+FPHs72dmYWZYW5TJCfw+8e8KhayferdStTKaDanpFi2RGxe3uZjMh3ZoKSxGugCxiAR/7cwIAAW1px4Z9ZmxhzFtQIp+Deyh871IVEjhOyYjs8QZtZCPGr6QAuH4wd1srXU1FJqU2lDGa/DYj4tBFl0bfO2oczJUuy5UJQWFZFbS7uBVAP5P5en2pvg04eqghlksCEQ/npzZolGSjSmobxlOf/8CXy9vYBNp12W1DlzC8RkQnNhC/3Mm8JWciSechunP4NfAig9QG5UJiy2WjeiwWnLyk4OpDaYA09hVjuxJ12xuScggkYwbPDdsyU1SftLynjoB6ktHamiFtg1DvuIqdfl8T4Qd8iGBTk00b49NoI9+LB9Yu6fNiGbYgMKzsr5LAStaAtvFApQolsreGjCUX6K62qr+0HmKX10esO+yi3cFvj20Yy/BS8xfLrakx4Z3f3wzk/0ONSHtoHLO/PbZTlLxtRypjenTnqV95dMRmBhjdS0RG9vUmvtEISN4CiFMTsMJU6QJg77Lmp5Tz7A=",
        new Perk[]{Perk.PROSPECTION, Perk.MINING_XP_BUFF, Perk.MINING_FIESTA, Perk.MOLTEN_FORGE}
    ),
    DIANA(
        "Diana", Kind.REGULAR,
        "ewogICJ0aW1lc3RhbXAiIDogMTU5Nzk0MjU3NDIzMCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODNjYzFjZjY3MmE0YjI1NDBiZTM0NmVhZDc5YWMyZDllZDE5ZDk1YjYwNzViZjk1YmUwYjZkMGRhNjEzNzdiZSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        "FepPcmM5MycgGDcxdEFXgQ0ncPuOWemDQRpJguSUc2Xfyz0Dub5OKDAiBOPkWDt7gR/UUOfenDW8XGg0gRFSvw4VR6maV1twwL3S+sn6rgIYfBZ4A5khrVpKNTvXQFDyMPB4780dsvIrM34CAcLzadxdoldT1Z8iibunDXVmLKiJwj9RctTRwdc3nbk/7a1P2pkpUs9JRUwbLwidZJRFbhSMr87jyNiUJzYKAaQ1vXpsWvkRD860PuLrbi8Nf1GInHuxRaIkeMRP6tQKDY7XubW2wIYrDn5MzZf9htnOBKWRx50s3e2qy3PiBgI7iJNbEo9X1qNHBQe6FkHS6zsZLX63kJDSyzq4+xgKZmSvVnPYFKeJLor/gKf+xX9RkVKzZqlU97jNHV+9vXUWfrVoUfCNpNwmK2YR+HT86Q5g0vQyD3gwq1W/QvvFkmgU7sOHRINVNxH6R6QOx3cKKDVaEqPjRKkEMsJOBLsJjNXDRMKg21oh+mhxzSRyjDy6LBBIPLM6m25GUtBhAuMs65gwWeyUAOml1hNLN9knzPVPigv0iAx8BJxvSIhXWkudA0UDDjnNGFGpxinykftWr1FAKo40M+SSnOG58mpOwGJEPxsWQm4k8IY73c1fiAdugh/LbkD2WBS0nc3qHQhZAW8xM7sZaBLhq7Et+9hrQx8qse8=",
        new Perk[]{Perk.HUNTRESS_INTUITION, Perk.MYTHOLOGICAL_RITUAL, Perk.PET_XP_BUFF, Perk.SHARING_IS_CARING}
    ),
    DIAZ(
        "Diaz", Kind.REGULAR,
        "ewogICJ0aW1lc3RhbXAiIDogMTU5Nzc4MTU4MDMzNSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWNmNDczN2NkNDQ0YjU5MDU0NTczNGE2NDA4Y2JlMjNjMTgyZjQyODNmMTY3YTNlM2MwOTUzMmNjYmVmMTdmOSIKICAgIH0KICB9Cn0=",
        "jdxgkeD/COv++whPWHAYgZjjiO71XLsU7Yqh/iTsZD1WygRxiGyo+xRYU6I3F7Hyn/3PhMyjwwzvhqELpjPvdENgyA/KWygDqLNjzb/yAhqJuitJVddnpdiSgevhda/Rpww6R6WhKfka403wSzKmZIwcGBufkRVJvsOOJa8yTqL3Dk2eHRc8EMDCsuIUIC3NhItcva1rxU4G6PeaZ84gYAYTyw7eAhLJY6ev84f4yUcyqdVM5PBu99xkBq7ui27FHBHEqvdH5pf/JJ/pKIbY7EqWJ67093TMKA6QtkX3gyaz+Vz4cH5opFu+52PE/oeLaRzH0i330SrXif0TJZgFoVKLQuqqvDinXwBTJmH+Ozf0wzADzwNoUuOUMfZ1R0c+aWieg2qrlGHzNXBhkhair/bIWE/4NraIURlNcGf8XfgZ1klIZyIcFT1N9T0utKSZXMAqeWL7Wo29khJ9OO5joJod6iRanJIRGQs7B85ZTxsTQ/17MU73U6fUgldAS+5zvMvM1u217SQCzaIdufC2G9QrNMkmKqHNWblj2AUD+pxs++FIokY4BhEQ0ZqSAUp7jncpy1Te6GmPEFr1uUfieVsUCzMIE6/QFiygRng708JGIhaXjJTrOyfaWwb7BXQ5rUkhXYtT4zciFeCrgO+b8JUjo6X3g/QvgOX1Ky9nwnc=",
        new Perk[]{Perk.STOCK_EXCHANGE, Perk.LONG_TERM_INVESTMENT, Perk.SHOPPING_SPREE, Perk.VOLUME_TRADING}
    ),
    FINNEGAN(
        "Finnegan", Kind.REGULAR,
        "ewogICJ0aW1lc3RhbXAiIDogMTY1NDIwMzAxNDc3MiwKICAicHJvZmlsZUlkIiA6ICI0NGNlMmMwZTFjNTM0ZDhmYmExNmNkNDhlYjkyYTUxZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4WGlyYW56dThYeCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNzc0N2ZiZWU5ZmIzOWJlMzliMDBkM2Q0ODNlYjJmODhiNGJhZTgyNDE3YWI1Y2IxYjFhYTkzMGRkN2I2Njg5IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
        "W1VnWYThZuihE6z8wX4XqfYwXIMxSHeXpWabQYntXkBeL+G4tA7Vu5p/IbBIco2w2uVeJZ1HC5HnHFQMgcBSXImHyomGjn4T2NQD9zg4sAAXevh8T9oeAC92qzCCa5wyyzm0hhxFhJ2RHr+JOii0cfaPn+7P6gLxZgVfaRLPJA4d5J+SwaUdTcHBVZm6A0Tzn54T4Acj4tUcWFZNrsqOck9VdJy9gLPvE3xDKDOVk4MTvXUJFRxOEjwWiIKmdP8ccDqy/C3F/zB0fulCXc0e8LWN9RKUQc0NSiz+TFetzZbNhi6PApA7DaicNKbZcj9DCDiGFvZUi2YMiadI25h7yhkF98uUc06Bg1soe8UTKok4LAFpt93CHr9ISFGXF34SnMSphbIWvsp34KgmdDOxAbSCogHDFfbozzljS2t07DHD0TiWfA+6WbzL6WB94774A/c8iPOFcPnmdBHodeYjp5bn8GBn+Bzu4dbOJxFmEj4940ZrYgYG1USwaw69hApXxmDJbzeD6oojiceT2CARGrFAxwpI9TvMU2ZtLATnS01vXxlX8NVcR+ovR1awuPOB/NiCY0kCxs/2pi65Glm09NqAK8AurFJXK/ueHYnV+FzuutSybvc78Mn2+iwgzOoxD0GM9fqw9eC70tI+10daitNyIi9QK0R6yfi3WpzxEzs=",
        new Perk[]{Perk.PEST_ERADICATOR, Perk.GOATED, Perk.BLOOMING_BUSINESS, Perk.GRAND_FEAST}
    ),
    FOXY(
        "Foxy", Kind.REGULAR,
        "ewogICJ0aW1lc3RhbXAiIDogMTU5Nzc4MTM1NTM0MywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzQ4NWE3MTdmYTBmNTFkN2ZhZGM2NmE1ZDVlOTg1MzkwNWJlZjkxNGUzYjI4NDhhMmYxMjhlNjNkMmRiODciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
        "hsdCpEijJf50Jx84oVf2b9sTv9JswZQ6rM+/sszeTpIDhuR9NH/3icxLz16i3PJ2Yvze7f6pd4KNfV2pqJhadO11rNhU3W0XGBAtd2bWJ1Ib16U0Qe1EHFY6g8r5VfRk1ZHBZxlRUQK15bd1MfuTMNr4+kl2BiCt7Ee/8dX+AWsNHXz0mbL4GRnrrBy9Y7bkBAWqOa3fjYnqCGzbmSi2yajxWqmUaba1qVuOA8ko/a1KkC/4yEoxd775gHZXyorZTD3VZEh6wbk9Y5eTeSo0U2fJCxmE18uZaxEm7uyMIpdV8QLH5VoHCvS+7liQ01BI6sITL8HwBINIW80oeSsOFLkB1E7yM185KPbMP3r0ahUuRfmSwiD37cqryMRvRWKlIdg9FojeKQENKXn9cbxSvZSjqerL01+Fp3Kw14iYU7/tIPX7VyAx/Qg0OYam3dTj4CLiEfwJc/NnE0p0mhaGQAiVDP3WJH7yjoHXlwZ1XIirJjWzUyzIz25gPGTR5Q/1WZjho8YQZmqqpNyzkX5yknHcS2iJIKj5wALsblqToy13Rsv1zAJiB6oyN5IfznOjBiHdcgpBApEGuCNQAAwNsUrQgJt2QvXniWCRBK9/SiUNa/9Sl5DoqrA72HgbM7Ty6j7lxBrx0BuLFUMw0Qtir5zzeM3Ndtc+IqeprJpkAdM=",
        new Perk[]{Perk.SWEET_BENEVOLENCE, Perk.A_TIME_FOR_GIVING, Perk.CHIVALROUS_CARNIVAL, Perk.EXTRA_EVENT}
    ),
    MARINA(
        "Marina", Kind.REGULAR,
        "ewogICJ0aW1lc3RhbXAiIDogMTU5Nzc4MTQxNjY5MiwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA3ZmM5YmVlOGQzMzQ0ZTg0MGU0MDMxYTM3MjQ5YTRjM2M4N2ZjODBjZjE2NDMyY2M1YzIxNTNkMWY5YzUzZCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
        "V6CFGoMGqrt0ZdwQnJSHAt8a93/loxkwtYjcp/oDDnX17YSCS9chIp6OzTvyOPyupgB81TXwS17iH2DdTQGQrquJToeyqDXPWCtnK6em8nfkDedsjovgD06BV1FbeWQ+JSNLCFL4M4GIiG2S2mpQZXXu1SboHnA+ZISYUR2qk9s75uDoden8TOB16OJBd6/pyxQ9tNQr0uQIIB5QKE83PiHaqAisFL/R/lWrOO2myRPDP41NTwjwJJthvC2q4/PUE+RBgbyr6JGBO2suchq7pRBfFKq99XCqyb40q4g+QLDaIRnQCLyi4IiYsLtws3WrIzqZ6PsPsFDbiUQClRXii2FTRSo804Oj4A4guPG//zNQ5EaJ8j9LoxZzrZt/wgYUJHI2LyM7f+o8lR3y3O4SmS9R3bQWiVRUQP6A8EKKsKpops1gCSgpZMshX+PKA2JCNt96zW1x1Vue7hvBRSbWxdPLBzrXQQ8pJi4gI2C13iP7D3W1quiKKHWZNAaZNKlx6zF7LVHTsXohPW6iP7nl5+tCzc7dp+5sPrV6UCJEVVjQ6FX1vAhFZMcNxjMi8zLLI83NDG7SEEaY3KHaX6R7VFE+lrsJq+PNdbwoaAQBxWQHBiMhFqqKj08/IjC35tFDqRKLG1kNhllWp0uGfx3NxIABJHUb9S+iSGIf4r+3EpI=",
        new Perk[]{Perk.FISHING_XP_BUFF, Perk.LUCK_OF_THE_SEA, Perk.FISHING_FESTIVAL, Perk.DOUBLE_TROUBLE}
    ),
    PAUL(
        "Paul", Kind.REGULAR,
        "ewogICJ0aW1lc3RhbXAiIDogMTU5Nzc4MDk0OTYzOCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI1OWM0M2Q4ZGJjY2ZkN2VjNmU2Mzk0YjYzMDRiNzBkNGVkMzE1YWRkMDQ5NGVlNzdjNzMzZjQxODE4YzczYSIKICAgIH0KICB9Cn0=",
        "UeEf0Ir81BLTBE0PfVeEtU/6mHUw3xSP7XGcQYN18qlYS6J7qd+bQskaSJQbEHXr+axq2+5aPm/AfGrjNnl9zQn1EucBIwhdRBHnjuJeRY6x9VKEHAtX2gnpnNelU/oP6MKPZ1dUad4iAHQg8BmJR/oQpedvOJDuqdUZowe8WTVFC5qctJQWRIZCX0BWYK1O1xxJx4FZ9LzF++7qWZsVqO+qmOR8R7Xr4jFkF8cdNIRyezgcfhmw3BCqiDDheOzuJzo0l7y9kHR82reHus/JBLGyTy/iqMqlZFgNePEoaOGRgIvROw9oIS4R/19+UABIe0MDD6CSGgsE7VfgWeyCVw3qxmCSAZDHnYuYyH0zpnGQRsmQrx9aTjFXHf6g551MEpx7KGGTMaOm9b7ygOGuVGB/52UXR2W9UU+YtBZoUzDPMeVcM1NQno/fY3rLoSf5PfuaaEpZMlczYpH0DBvApEQ6FBm/XTOPeS5w3a+7UXh/wrjXu5b62rXG4SNZaBZRT76eseX3wZoCGiLYEh+IXfAJxXSKOqmlHmRmv6FGRzMcZpHzr63GqD0jJwpLuUZ54uuMgUtjx/liEZG1pesdUuf0ObRVf+xDxk/iLAiNKRvd6BoP7wVm0CkebDlyPObdKW0Ss+tAhB0y1o4das+n+UjpHUuPM2D/eLZFA01umyw=",
        new Perk[]{Perk.MARAUDER, Perk.BENEDICTION, Perk.EZPZ}
    ),
    BARRY(
        "Barry", Kind.RETIRED,
        "ewogICJ0aW1lc3RhbXAiIDogMTU5Nzc4MDg1NjQ5NCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjA0YzU5MWIxNjQ3NDZlODQ4ZjNkNmE0NTFlZTg3YTYyZGQxOTNlNWM0NWU5NGVkNzhlNzJkZjExOWFjYTQyNiIKICAgIH0KICB9Cn0=",
        null,
        new Perk[]{Perk.ARCANE_CATALYST, Perk.ASTRAL_NEGOTIATOR, Perk.MAGIC_XP_BOOST}
    ),
    SCORPIUS(
        "Scorpius", Kind.SPECIAL,
        "ewogICJ0aW1lc3RhbXAiIDogMTU5Nzc4MTc1NzIxOSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGYyNmZhMGM0NzUzNmU3OGUzMzcyNTdkODk4YWY4YjFlYmM4N2MwODk0NTAzMzc1MjM0MDM1ZmYyYzdlZjhmMCIKICAgIH0KICB9Cn0=",
        null,
        new Perk[]{Perk.BRIBE, Perk.DARKER_AUCTIONS}
    ),
    DERPY(
        "Derpy", Kind.SPECIAL,
        "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjQ1MGQxMjY5Mjg4NmM0N2IwNzhhMzhmNGQ0OTJhY2ZlNjEyMTZlMmQwMjM3YWI4MjQzMzQwOWIzMDQ2YjQ2NCJ9fX0=",
        null,
        new Perk[]{Perk.QUAD_TAXES, Perk.TURBO_MINIONS, Perk.DOUBLE_MOBS_HP, Perk.MOAR_SKILLZ}
    ),
    JERRY(
        "Jerry", Kind.SPECIAL,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODIyZDhlNzUxYzhmMmZkNGM4OTQyYzQ0YmRiMmY1Y2E0ZDhhZThlNTc1ZWQzZWIzNGMxOGE4NmU5M2IifX19",
        null,
        new Perk[]{Perk.PERKPOCALYPSE, Perk.STATSPOCALYPSE, Perk.JERRYPOCALYPSE}
    ),
    DANTE(
        "Dante", Kind.ONE_TIME,
        "ewogICJ0aW1lc3RhbXAiIDogMTYxODk2NjA2MDYyMSwKICAicHJvZmlsZUlkIiA6ICI0NWY3YTJlNjE3ODE0YjJjODAwODM5MmRmN2IzNWY0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfSnVzdERvSXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFiZDE5YWVmZGZkYWM3ZTI3ZGVhNjIxNjZiNDFmOWMxMzQ4NDFhMjkwNmY3MDRmMjJkMGQzY2QwNGNmM2MwZiIKICAgIH0KICB9Cn0=",
        "bB1TQWe6O950wmZ8VEcR7p8ZONgPXIW8EzryTeYaw6bXzRDolpzbNr2Fz4MxzX5/dvBS5V18xEFl/6qf4oCorYopWSejJ0D3q9T8BLBjpYqmfx+K2aQjXpWiSmK7/i0ecsbGc0TeJbl6Q/8YYSQ2DqcF6J/5TJYNErs/XJvcNtx/XAleYJzLVxEgZ7CMti6OkopJyYZCyobEKG8ZHvJH4zEkYvPBnU+rAjEVRA+ai1WxQ96WJESF6amw4VCAeGn7jiTPQktU14ifcwBA8be+sCOkdV5J0Dimv46bk+N/LNdnCY5H9vDgsV4ymLqq2aX5/FY6y28Z1XnHR/S7I0IvyQdZbsGLV23eGFxQUJ13hjooVW7cc2cCm39PMDuji7ycL0IXkbbEVNyYuBBkXBaf6jB6+NefIvpUBFhN9Llfncw6SUKK9cUeaph2j8m8XH2RLeY5qBriUwdL0EBOJGsUgEDP9phWAeFb2BmsCONYYbFEI2cO4+rg3PYIJuNgrsamv70X1SYyy4xvDCN3J4RuJqT+rR67leJzeBje3cj1wcUbjecquODIUAM7PZcmU60gMTExOv5BTOxfkgoWWgtcYRVy+Zn7MTx93JllIGMDqqOQey1g6uKRujZqtYgX50a6TwByQPf1uAMHx5TXDuVi3wmpmjufsw6cQRuOqGjVUMs=",
        new Perk[]{Perk.ECONOMY_RESTORATION, Perk.INCREASED_SECURITY}
    ),
    TECHNOBLADE(
        "Technoblade", Kind.ONE_TIME,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWI4YjUxZWFlZmUwZTNhMTBhZDdmYTE2Mjc4MDFkMmNlNDQxMmI0ZTQzMjY4NTkwNjE5MmY4MzI4MzcwODIzMyJ9fX0=",
        null,
        new Perk[]{Perk.BLOOD_GODS_BLESSING, Perk.SHRINE_OF_THE_BLOOD_GOD, Perk.UNLIMITED_SPEED, Perk.POTATO_CROWNS_RADIANCE, Perk.ANARCHY}
    );

    private final String displayName;
    private final Kind kind;
    private final String texture;
    private final String signature;
    private final Perk[] allPerks;

    SkyBlockMayor(String displayName, Kind kind, String texture, String signature, Perk[] allPerks) {
        this.displayName = displayName;
        this.kind = kind;
        this.texture = texture;
        this.signature = signature;
        this.allPerks = allPerks;
    }

    public static List<SkyBlockMayor> getRegularMayors() {
        return Arrays.stream(values()).filter(m -> m.kind == Kind.REGULAR).toList();
    }

    public static List<SkyBlockMayor> getSpecialMayors() {
        return Arrays.stream(values()).filter(m -> m.kind == Kind.SPECIAL).toList();
    }

    public static List<SkyBlockMayor> getOneTimeMayors() {
        return Arrays.stream(values()).filter(m -> m.kind == Kind.ONE_TIME).toList();
    }

    public boolean isSpecial() {
        return kind == Kind.SPECIAL;
    }

    public boolean canBeMinister() {
        return kind == Kind.REGULAR;
    }

    public boolean isOneTime() {
        return kind == Kind.ONE_TIME;
    }

    public enum Kind {
        REGULAR,
        SPECIAL,
        ONE_TIME,
        RETIRED
    }

    @Getter
    public enum Perk {
        SLASHED_PRICING("SLASHED Pricing", "<7>Starting slayer quests is <e>half price<7>."),
        SLAYER_XP_BUFF("Slayer XP Buff", "<7>Earn <d>25% <7>more Slayer XP."),
        PATHFINDER("Pathfinder", "<7>Gain rare drops <d>20% <7>more often."),

        PROSPECTION("Prospection", "<7>Mining minions work <a>25% <7>faster."),
        MINING_XP_BUFF("Mining XP Buff", "<7>Get <3>+60☯ Mining Wisdom <7>on public islands."),
        MINING_FIESTA("Mining Fiesta", "<7>The <6>Mining Fiesta <7>lasts the whole term! On public islands gain <3>+25☯ Mining Wisdom <7>and <6>+25☘ Mining Fortune<7>, and collect <9>Refined Mineral <7>and <5>Glossy Gemstone <7>from mining!"),
        MOLTEN_FORGE("Molten Forge", "<7>Decrease the time it takes to <6>forge items <7>by <d>25%<7>."),

        HUNTRESS_INTUITION("Huntress' Intuition", "<7>Gain <d>+10❃ Tracking<7>."),
        MYTHOLOGICAL_RITUAL("Mythological Ritual", "<7>Mayor Diana will sell the Griffin pet, which lets you find <2>Mythological Creatures <7>and tons of <e>unique items<7>."),
        PET_XP_BUFF("Pet XP Buff", "<7>Gain <d>35% <7>more pet XP."),
        SHARING_IS_CARING("Sharing is Caring", "<7>Unlocks <a>2 <7>more <6>Exp Share <7>slots in your pets menu and increases the <6>Exp Share <7>rate by <a>+10%<7>!"),

        LONG_TERM_INVESTMENT("Long Term Investment", "<7>The <b>elected minister <7>will appear as a candidate with <d>all <7>of their perks in the <a>next election cycle<7>."),
        VOLUME_TRADING("Volume Trading", "<7>The available item quantity per <6>Shen's Auction <7>has been doubled, and two additional <6>Shen's Special <7>auctions will be available if Diaz is Mayor."),
        STOCK_EXCHANGE("Stock Exchange", "<7>Participate in the <d>Stonks Auction <7>for a chance to win <5>Stock of Stonks<7>! Trade them for <6>extravagant items <7>at the <b>Trade Center<7>."),
        SHOPPING_SPREE("Shopping Spree", "<7>Increase daily NPC buy limits by <e>10x<7>."),

        PEST_ERADICATOR("Pest Eradicator", "<7>The duration of Pesthunter Phillip's <6>☘ Farming Fortune <7>bonus is now <a>60m<7>. Decreases the spawn cooldown of <2>Pests <7>by <a>20%<7>."),
        GOATED("GOATed", "<e>Jacob's Farming Contest <7>brackets include <a>10% <7>more players each."),
        BLOOMING_BUSINESS("Blooming Business", "<7>All Garden Visitors reward <a>Fine Flour <7>and <c>+10% Copper <7>for accepting offers. Invites <a>5 <7>unique visitors to your <b>Garden <7>and makes higher rarity visitors <a>20% <7>more likely to show up."),
        GRAND_FEAST("Grand Feast", "<7>Finnegan upgrades the <6>Harvest Feast <7>into the <d>Grand Feast<7>, making it lasts a full SkyBlock year. Also opens the <c>Grand Bakery<7>, where <6>Kernels <7>can be spent on more exclusive items."),

        SWEET_BENEVOLENCE("Sweet Benevolence", "<7>Earn <a>+30% <7>more <5>Candy<7>, <c>Gifts <7>and <6>Chocolate <7>from duplicate Rabbits during their respective events."),
        A_TIME_FOR_GIVING("A Time for Giving", "<7>Spawn <d>Party Chests <7>by killing mobs during the <6>Spooky Festival <7>and collect <b>Party Gifts <7>from <c>Jerry's Workshop<7>."),
        CHIVALROUS_CARNIVAL("Chivalrous Carnival", "<7>Schedules a <e>Carnival <7>in the hub, active throughout the <b>entire year<7>."),
        EXTRA_EVENT("Extra Event", "<7>Schedules an extra <6>Spooky Festival <7>event during the year."),

        FISHING_XP_BUFF("Fishing XP Buff", "<7>Gain <3>+50☯ Fishing Wisdom <7>on public islands."),
        LUCK_OF_THE_SEA("Luck of the Sea 2.0", "<7>Gain <3>15α Sea Creature Chance<7>."),
        FISHING_FESTIVAL("Fishing Festival", "<7>Start a special fishing event the first <b>3 <7>days of each month! Fish and fight dangerous <b>sharks <7>and earn unique Shark loot."),
        DOUBLE_TROUBLE("Double Trouble", "<7>For every <3>1 α Sea Creature Chance<7>, gain <9>+0.1 ⚓ Double Hook Chance<7>."),

        MARAUDER("Marauder", "<7>Dungeon reward chests are <e>20% <7>cheaper."),
        EZPZ("EZPZ", "<7>Gain <e>10 <7>bonus score on dungeon runs."),
        BENEDICTION("Benediction", "<7>Blessings are <d>25% <7>stronger."),

        ARCANE_CATALYST("Arcane Catalyst", "<7>Spells deal <c>15% <7>increased damage."),
        ASTRAL_NEGOTIATOR("Astral Negotiator", "<7>Enchanting and anvils costs <b>-15% <7>less experience."),
        MAGIC_XP_BOOST("Magic XP Boost", "<7>Gain <a>+15 <3>☯ Alchemy Wisdom <7>and <3>☯ Enchanting Wisdom<7>."),

        BRIBE("Bribe", "<7>If Scorpius wins and you voted for him, Mayor Scorpius will offer you <6>coins <7>as a token of gratitude."),
        DARKER_AUCTIONS("Darker Auctions", "<7>Scorpius will intrude in Dark Auctions, increasing the amount of rounds to 7 and offering special items."),

        QUAD_TAXES("QUAD TAXES!!!", "<f>Pay 4x the normal amount of taxes!"),
        TURBO_MINIONS("TURBO MINIONS!!!", "<7>Minions have double the output!"),
        DOUBLE_MOBS_HP("DOUBLE MOBS HP!!!", "<7>ALL monsters have double health!"),
        MOAR_SKILLZ("MOAR SKILLZ!!!", "<7>Gain <d>+50% <7>more skill experience!"),

        PERKPOCALYPSE("Perkpocalypse", "<7>Activates all perks of another mayor every 18 SkyBlock days (6 hours)"),
        STATSPOCALYPSE("Statspocalypse", "<7>Increases all <c>Combat <7>and <d>Miscellaneous <7>stats by <a>10%<7>."),
        JERRYPOCALYPSE("Jerrypocalypse", "<7>Reveal hidden Jerries from logging, farming, mining and killing mobs."),

        ECONOMY_RESTORATION("Economy Restoration", "<7>Auction House and Bazaar taxes are <c>doubled<7>, NPC shops charge a <c>10% <7>coin tax, chatting costs <6>5 coins <7>per minute and travelling between islands costs <6>2 coins<7>."),
        INCREASED_SECURITY("Increase Security", "<7>Goons guard the official buildings and only let in people wearing a <f>Happy Mask <7>or holding <f>Dante's Talisman<7>."),

        BLOOD_GODS_BLESSING("Blood God's Blessing", "<7>Gain <d>+50% <7>more skill experience!"),
        SHRINE_OF_THE_BLOOD_GOD("Shrine of the Blood God", "<7>Summons the <c>Blood God Shrine <7>with its <d>Ancient Pig <7>and <d>Shiny Pigs<7>."),
        UNLIMITED_SPEED("Unlimited Speed", "<7>Gain <f>+50✦ Speed <7>and remove the speed cap."),
        POTATO_CROWNS_RADIANCE("Potato Crown's Radiance", "<7>Farming minions have double the output!"),
        ANARCHY("Anarchy", "<7>There are <a>no taxes <7>on the <6>Auction House <7>and the <6>Bazaar<7>.");

        private final String displayName;
        private final String description;

        Perk(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
    }
}
