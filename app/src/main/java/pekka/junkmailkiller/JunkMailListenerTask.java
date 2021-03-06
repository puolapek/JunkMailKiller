package pekka.junkmailkiller;

import android.os.AsyncTask;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.BodyTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SubjectTerm;

/* NOT IN USE */
public class JunkMailListenerTask extends AsyncTask<Object,Void,Void> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Void doInBackground(Object... params) {

        try {
            boolean settingsOK;
            Properties properties = new Properties();
            Session emailSession = Session.getDefaultInstance(properties);
            Store store = emailSession.getStore("imap");
            Settings settings = (Settings)params[0];

            store.connect(settings.getHost(), settings.getUser(), settings.getPassword());

            Folder fromFolder = store.getFolder("INBOX");
            fromFolder.open(Folder.READ_WRITE);

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static OrTerm setSearchTerms(Settings settings) {
        int NUMBER_OF_KEYWORDS = settings.getKeyWords().size();
        SubjectTerm[] subjectTerms = new SubjectTerm[NUMBER_OF_KEYWORDS];
        BodyTerm[] bodyTerms = new BodyTerm[NUMBER_OF_KEYWORDS];
        FromStringTerm[] fromStringTerms = new FromStringTerm[NUMBER_OF_KEYWORDS];

        for (int i = 0; i < NUMBER_OF_KEYWORDS; i++) {
            FromStringTerm fromStringTerm = new FromStringTerm(settings.getKeyWords().get(i));
            fromStringTerms[i] = fromStringTerm;

            SubjectTerm subjectTerm = new SubjectTerm(settings.getKeyWords().get(i));
            subjectTerms[i] = subjectTerm;

            BodyTerm bodyTerm = new BodyTerm(settings.getKeyWords().get(i));
            bodyTerms[i] = bodyTerm;
        }

        Flags seen = new Flags(Flags.Flag.SEEN);
        //FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        OrTerm subjectOrTerms = new OrTerm(subjectTerms);
        OrTerm bodyOrTerms = new OrTerm(bodyTerms);
        OrTerm fromStringOrTerms = new OrTerm(fromStringTerms);
        OrTerm subjectBodyOrterms = new OrTerm(subjectOrTerms, bodyOrTerms);
        OrTerm allOrTerms = new OrTerm(subjectBodyOrterms, fromStringOrTerms);
        //AndTerm searchTerm = new AndTerm(unseenFlagTerm, allOrTerms);

        return allOrTerms;
    }


    private boolean createFolder(Folder folder)
    {
        boolean isCreated;

        try
        {
            isCreated = folder.create(Folder.HOLDS_MESSAGES);

        } catch (Exception e)
        {
            System.out.println("Error creating folder: " + e.getMessage());
            e.printStackTrace();
            isCreated = false;
        }
        return isCreated;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        System.out.println("onPostExecute...");
        super.onPostExecute(aVoid);
    }
}
