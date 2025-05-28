package soundtribe.soundtribeusers.exceptions;

public class SoundtribeUserValidationException extends SoundtribeUserException {

    public SoundtribeUserValidationException(String exMessage) {
        super("Soundtribe User Validation Exception: " + exMessage);
    }

}
