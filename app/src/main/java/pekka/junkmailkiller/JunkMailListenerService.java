package pekka.junkmailkiller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.BodyTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SubjectTerm;


public class JunkMailListenerService extends Service {

    private final String junkMailFolder = "JUNK_MAIL_KILLER";
    Folder fromFolder;
    Folder toFolder;
    Store store;
    int frequence;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        DBHelper dbHelper = new DBHelper(this);
        final Settings settings = dbHelper.readSettings();

        Thread thread = new Thread(new Runnable(){

            @Override
            public void run() {

                try {

                    Properties properties = new Properties();
                    Session emailSession = Session.getDefaultInstance(properties);
                    store = emailSession.getStore("imap");
                    try {
                        frequence = Integer.parseInt(settings.getFreq());
                    } catch (NumberFormatException e) {
                        frequence = 5;
                    }

                    store.connect(settings.getHost(), settings.getUser(), settings.getPassword());

                    fromFolder = store.getFolder("INBOX");
                    fromFolder.open(Folder.READ_WRITE);

                    toFolder = store.getFolder(junkMailFolder);
                    if (!toFolder.exists()) {
                        createFolder(toFolder);
                    }
                    toFolder.open(Folder.READ_WRITE);
                    Message[] toFolderMessages = new Message[1];
                    OrTerm searchTerm = setSearchTerms(settings);

                    for (;;) {

                        Message[] junkMails = fromFolder.search(searchTerm);

                        for (Message message : junkMails) {
                            toFolderMessages[0] = message;
                            fromFolder.copyMessages(toFolderMessages, toFolder);
                            message.setFlag(Flags.Flag.DELETED, true);
                        }

                        fromFolder.close(true);
                        toFolder.close(true);
                        store.close();

                        try {
                            TimeUnit.SECONDS.sleep(frequence);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Reopen
                        store.connect(settings.getHost(), settings.getUser(), settings.getPassword());
                        fromFolder.open(Folder.READ_WRITE);
                        toFolder.open(Folder.READ_WRITE);

                    }

                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (MessagingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        thread.start();

        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
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

        //Flags seen = new Flags(Flags.Flag.SEEN);
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

}
