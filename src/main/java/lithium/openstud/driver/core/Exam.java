package lithium.openstud.driver.core;

import java.util.Objects;

public abstract class Exam {
    private String description;
    private String examCode;
    private String ssd;
    private int cfu;

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public String getExamCode() {
        return examCode;
    }

    protected void setExamCode(String examCode) {
        this.examCode = examCode;
    }

    public String getSsd() {
        return ssd;
    }

    protected void setSsd(String ssd) {
        this.ssd = ssd;
    }

    public int getCfu() {
        return cfu;
    }

    protected void setCfu(int cfu) {
        this.cfu = cfu;
    }
}
