package pekka.junkmailkiller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SubjectTerm;


public class JunkMailListenerService extends Service {
    private boolean running = true;
    private Thread thread;
    private PowerManager pm;
    private PowerManager.WakeLock wl;
    private final String junkMailFolder = "JUNK_MAIL_KILLER";
    private Folder fromFolder;
    private Folder toFolder;
    private Store store;
    private int frequence;

    @Override
    public void onCreate() {
        super.onCreate();
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "JunkMailListenerService");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final DBHelper dbHelper = new DBHelper(this);
        final Settings settings = dbHelper.readSettings();

        thread = new Thread(new Runnable(){

            @Override
            public void run() {

                try {
                    // Wakelock on.
                    wl.acquire();

                    Properties properties = new Properties();
                    Session emailSession = Session.getDefaultInstance(properties);
                    store = emailSession.getStore("imap");
                    try {
                        frequence = Integer.parseInt(settings.getFreq());
                    } catch (NumberFormatException e) {
                        frequence = 5;
                    }

                    dbHelper.insertOrUpdateSettingsOK("false");
                    store.connect(settings.getHost(), settings.getUser(), settings.getPassword());
                    dbHelper.insertOrUpdateSettingsOK("true");

                    fromFolder = store.getFolder("INBOX");
                    fromFolder.open(Folder.READ_WRITE);

                    toFolder = store.getFolder(junkMailFolder);
                    if (!toFolder.exists()) {
                        createFolder(toFolder);
                    }
                    toFolder.open(Folder.READ_WRITE);
                    Message[] toFolderMessages = new Message[1];

                    OrTerm keywordsTerm = setKeywordTerms(settings);
                    OrTerm exceptionsTerm = setExceptionTerms(settings);
                    NotTerm notExceptionTerm = new NotTerm(exceptionsTerm);
                    AndTerm searchTerm = new AndTerm(keywordsTerm, notExceptionTerm);

                    while (running) {

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

                }  catch (Exception e) {
                    e.printStackTrace();
                    if (wl.isHeld()) {
                        wl.release();
                    }
                }
            }
        });

        thread.start();

        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Stop thread.
        running = false;

        // Wakelock off.
        if (wl.isHeld()) {
            wl.release();
        }

        super.onDestroy();

        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    public static OrTerm setKeywordTerms(Settings settings) {
        int NUMBER_OF_KEYWORDS = settings.getKeyWords().size();
        SubjectTerm[] subjectTerms = new SubjectTerm[NUMBER_OF_KEYWORDS];
        BodyTerm[] bodyTerms = new BodyTerm[NUMBER_OF_KEYWORDS];
        FromStringTerm[] fromTerms = new FromStringTerm[NUMBER_OF_KEYWORDS];

        for (int i = 0; i < NUMBER_OF_KEYWORDS; i++) {
            FromStringTerm fromStringTerm = new FromStringTerm(settings.getKeyWords().get(i));
            fromTerms[i] = fromStringTerm;

            SubjectTerm subjectTerm = new SubjectTerm(settings.getKeyWords().get(i));
            subjectTerms[i] = subjectTerm;

            BodyTerm bodyTerm = new BodyTerm(settings.getKeyWords().get(i));
            bodyTerms[i] = bodyTerm;
        }

        //Flags seen = new Flags(Flags.Flag.SEEN);
        //FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        OrTerm subjectOrTerms = new OrTerm(subjectTerms);
        OrTerm bodyOrTerms = new OrTerm(bodyTerms);
        OrTerm fromStringOrTerms = new OrTerm(fromTerms);
        OrTerm subjectBodyOrterms = new OrTerm(subjectOrTerms, bodyOrTerms);
        OrTerm allOrTerms = new OrTerm(subjectBodyOrterms, fromStringOrTerms);
        //AndTerm searchTerm = new AndTerm(unseenFlagTerm, allOrTerms);

        return allOrTerms;
    }

    public static OrTerm setExceptionTerms(Settings settings) {
        int NUMBER_OF_EX_KEYWORDS = settings.getExKeyWords().size();
        SubjectTerm[] subjectTerms = new SubjectTerm[NUMBER_OF_EX_KEYWORDS];
        BodyTerm[] bodyTerms = new BodyTerm[NUMBER_OF_EX_KEYWORDS];
        FromStringTerm[] fromTerms = new FromStringTerm[NUMBER_OF_EX_KEYWORDS];

        for (int i = 0; i < NUMBER_OF_EX_KEYWORDS; i++) {
            FromStringTerm fromStringTerm = new FromStringTerm(settings.getExKeyWords().get(i));
            fromTerms[i] = fromStringTerm;

            SubjectTerm subjectTerm = new SubjectTerm(settings.getExKeyWords().get(i));
            subjectTerms[i] = subjectTerm;

            BodyTerm bodyTerm = new BodyTerm(settings.getExKeyWords().get(i));
            bodyTerms[i] = bodyTerm;
        }

        OrTerm subjectOrTerms = new OrTerm(subjectTerms);
        OrTerm bodyOrTerms = new OrTerm(bodyTerms);
        OrTerm fromStringOrTerms = new OrTerm(fromTerms);
        OrTerm subjectBodyOrterms = new OrTerm(subjectOrTerms, bodyOrTerms);
        OrTerm allOrTerms = new OrTerm(subjectBodyOrterms, fromStringOrTerms);

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
