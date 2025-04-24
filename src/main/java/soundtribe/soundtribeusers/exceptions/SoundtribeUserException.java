package soundtribe.soundtribeusers.exceptions;

public class SoundtribeUserException extends RuntimeException {

    public SoundtribeUserException(String exMessage) {
        super("Soundtribe User Exception: " + exMessage);
    }
}
