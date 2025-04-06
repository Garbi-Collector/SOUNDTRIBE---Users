package gabri.dev.javaspringcompose.models.enums;

import gabri.dev.javaspringcompose.exceptions.SoundtribeUserRolException;

import java.util.Locale;

public enum Rol {
    ADMIN,
    ARTISTA,
    OYENTE;

    /**
     * Convierte un string (como "admin", "Artista", etc.) al enum Rol correspondiente.
     * Ignora mayúsculas/minúsculas.
     */
    public static Rol fromString(String value) {
        if (value == null) throw new IllegalArgumentException("El valor no puede ser null");

        return switch (value.toLowerCase(Locale.ROOT)) {
            case "admin" -> ADMIN;
            case "artista" -> ARTISTA;
            case "oyente" -> OYENTE;
            default -> throw new SoundtribeUserRolException("Rol desconocido: " + value);
        };
    }

    /**
     * Convierte el enum a una cadena en minúsculas (por ejemplo: Rol.ADMIN -> "admin").
     */
    public String toLower() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
