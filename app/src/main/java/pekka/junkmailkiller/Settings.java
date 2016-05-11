/**
 * Created by Pekka on 1.4.2016.
 */
package pekka.junkmailkiller;

import java.util.ArrayList;

public class Settings {
    private String host;
    private String user;
    private String password;
    private String settingsOK;
    private String freq;
    private ArrayList<String> keyWords;
    private ArrayList<String> exKeyWords;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFreq() {
        return freq;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }


    public ArrayList<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(ArrayList<String> keyWords) {
        this.keyWords = keyWords;
    }

    public String getSettingsOK() {
        return settingsOK;
    }

    public void setSettingsOK(String settingsOK) {
        this.settingsOK = settingsOK;
    }

    public ArrayList<String> getExKeyWords() {
        return exKeyWords;
    }

    public void setExKeyWords(ArrayList<String> exKeyWords) {
        this.exKeyWords = exKeyWords;
    }
}
