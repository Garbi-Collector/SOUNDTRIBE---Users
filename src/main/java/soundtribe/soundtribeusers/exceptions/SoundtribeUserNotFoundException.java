package soundtribe.soundtribeusers.exceptions;

public class SoundtribeUserNotFoundException extends SoundtribeUserException {

    public SoundtribeUserNotFoundException(String exMessage) {
        super("Soundtribe User Not Found Exception: " + exMessage);
    }

}
