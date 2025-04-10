package gabri.dev.javaspringcompose.exceptions;

public class SoundtribeUserRolException extends SoundtribeUserException{
    public SoundtribeUserRolException(String exMessage) {
        super("Soundtribe User - Rol Exception: "+exMessage);
    }

}
