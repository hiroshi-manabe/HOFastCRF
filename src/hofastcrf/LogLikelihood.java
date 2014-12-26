package hofastcrf;

public class LogLikelihood {
    
    private double logLikelihood;
    
    LogLikelihood(double logLikelihood) {
        this.logLikelihood = logLikelihood;
    }
    
    void addLogLikelihood(double logLikelihood) {
        this.logLikelihood += logLikelihood;
    }
    
    double getLogLikelihood() {
        return logLikelihood;
    }
}
