package soundtribe.soundtribeusers.exceptions;

public class SoundtribeUserEmailException extends SoundtribeUserException {

    public SoundtribeUserEmailException(String exMessage) {
        super("Soundtribe User Email Exception: " + exMessage);
    }

}
