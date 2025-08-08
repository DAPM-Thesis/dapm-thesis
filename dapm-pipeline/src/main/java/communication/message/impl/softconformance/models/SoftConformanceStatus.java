package communication.message.impl.softconformance.models;

import java.util.Date;

import communication.message.impl.softconformance.models.pdfa.PDFA;
import org.apache.commons.math3.stat.descriptive.moment.Mean;


public class SoftConformanceStatus {
    private PDFA model;
    private String caseID;
    private String lastAct = null;
    private double lastProb = 0;
    private double prob = 1;
    private double logProb = 1;
    private Mean mean = new Mean();
    private long lastUpdate = System.currentTimeMillis();

    public SoftConformanceStatus(PDFA model, String caseID) {
        this.model = model;
        this.caseID = caseID;
    }

    public SoftConformanceStatus(PDFA model, String caseID, String lastAct, double lastProb, double prob, double logProb, Mean mean, long lastUpdate) {
        this.model = model;
        this.caseID = caseID;
        this.lastAct = lastAct;
        this.lastProb = lastProb;
        this.prob = prob;
        this.logProb = logProb;
        this.mean = mean;
        this.lastUpdate = lastUpdate;
    }

    public void replayEvent(String eventName) {
        if (lastAct != null) {
            lastProb = model.getSequenceProbability(lastAct, eventName);
            prob *= lastProb;
            logProb += -Math.log(lastProb);
            mean.increment(lastProb);
        }

        lastUpdate = System.currentTimeMillis();
        lastAct = eventName;
    }

    public PDFA getModel() { return model; }
    public String getLastAct() { return lastAct; }
    public double getLastProbability() {
        return lastProb;
    }
    public double getSequenceProbability() {
        return prob;
    }
    public double getSequenceLogProbability() {
        return logProb;
    }
    public double getMeanProbabilities() {
        return mean.getResult();
    }
    public Mean getMean() { return mean; }
    public long getLastUpdateValue() { return lastUpdate; }

    public double getSoftConformance() {
        double meanLocal = getMeanProbabilities();
        double nodes = model.getNodes().size();
        double weight = model.getWeightFactor();
        double best = weight + ((1d / nodes) * (1d - weight));
        return meanLocal / best;
    }

    public Date getLastUpdate() {
        return new Date(lastUpdate);
    }

    public String getCaseID() {
        return caseID;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SoftConformanceStatus) && caseID.equals(((SoftConformanceStatus) obj).caseID);
    }

    @Override
    public int hashCode() {
        return caseID.hashCode();
    }

    @Override
    public String toString() {
        return "soft conformance: " + getSoftConformance() + ", mean of probabilities: " + getMeanProbabilities();
    }

}
