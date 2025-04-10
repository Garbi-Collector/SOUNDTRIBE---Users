package gabri.dev.javaspringcompose.exceptions;

public class SoundtribeUserJWTException extends SoundtribeUserException {

    public SoundtribeUserJWTException(String exMessage) {
        super("Soundtribe User JWT Exception: "+exMessage);
    }

}
