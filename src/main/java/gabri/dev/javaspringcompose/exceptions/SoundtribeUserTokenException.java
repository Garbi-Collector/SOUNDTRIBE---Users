package gabri.dev.javaspringcompose.exceptions;

public class SoundtribeUserTokenException extends SoundtribeUserException {
    public SoundtribeUserTokenException(String exMessage) {
        super("Soundtribe User Token Exception: " + exMessage);
    }

}
