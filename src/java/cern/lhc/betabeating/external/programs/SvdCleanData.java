package cern.lhc.betabeating.external.programs;

import cern.lhc.betabeating.external.interfaces.ProgramData;

public class SvdCleanData implements ProgramData{
    private String turn;
    private String p;
    private String sumsquare;
    private String sing_val;
    private String file;
    private String std_dev;
    private String outputPath;
    
    private SvdCleanData() {
    }
    
    @Override
    public String getArguments() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                     .append(" --turn=").append(turn)
                     .append(" --p=").append(p)
                     .append(" --sumsquare=").append(sumsquare)
                     .append(" --sing_val=").append(sing_val)
                     .append(" --file=").append(file)
                     ;
        if (std_dev != null)
            stringBuilder.append(" --std_dev=").append(std_dev);
        return stringBuilder.toString();
    }
    
    public static SvdCleanDataCreate prepareObject()
    {
        return new SvdCleanDataCreate(new SvdCleanData());
    }
    
    private void setTurn(String turn) {
        this.turn = turn;
    }

    private void setP(String p) {
        this.p = p;
    }

    private void setSumSquare(String sumsquare) {
        this.sumsquare = sumsquare;
    }

    private void setSing_val(String sing_val) {
        this.sing_val = sing_val;
    }

    private void setFile(String file) {
        this.file = file;
    }

    private void setStd_dev(String std_dev) {
        this.std_dev = std_dev;
    }
    
    private void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getTurn() {
        return turn;
    }

    public String getP() {
        return p;
    }

    public String getSumSquare() {
        return sumsquare;
    }

    public String getSing_val() {
        return sing_val;
    }

    public String getFile() {
        return file;
    }

    public String getStd_dev() {
        return std_dev;
    }
    
    public String getOutputPath() {
        return outputPath;
    }

    public static class SvdCleanDataCreate
    {
        private SvdCleanData svdCleanData;
        private boolean isSetTurn;
        private boolean isSetP;
        private boolean isSetSumSquare;
        private boolean isSetSing_val;
        private boolean isSetFile;
        private boolean isSetOutputPath;
        
        private SvdCleanDataCreate(SvdCleanData svdCleanData) {
            this.svdCleanData = svdCleanData;
        }
        
        public SvdCleanDataCreate setTurn(String turn) {
            if (turn == null)
                throw new IllegalArgumentException("argument null");
            svdCleanData.setTurn(turn);
            isSetTurn = true;
            return this;
        }
        
        public SvdCleanDataCreate setP(String p) {
            if (p == null)
                throw new IllegalArgumentException("argument null");
            svdCleanData.setP(p);
            isSetP = true;
            return this;
        }
        
        public SvdCleanDataCreate setSumSquare(String sumsquare) {
            if (sumsquare == null)
                throw new IllegalArgumentException("argument null");
            svdCleanData.setSumSquare(sumsquare);
            isSetSumSquare = true;
            return this;
        }
        
        public SvdCleanDataCreate setSing_val(String sing_val) {
            if (sing_val == null)
                throw new IllegalArgumentException("argument null");
            svdCleanData.setSing_val(sing_val);
            isSetSing_val = true;
            return this;
        }
        
        public SvdCleanDataCreate setFile(String file) {
            if (file == null)
                throw new IllegalArgumentException("argument null");
            svdCleanData.setFile(file);
            isSetFile = true;
            return this;
        }
        
        public SvdCleanDataCreate setStd_dev(String std_dev) {
            if (std_dev == null)
                throw new IllegalArgumentException("argument null");
            svdCleanData.setStd_dev(std_dev);
            return this;
        }
        
        public SvdCleanDataCreate setOutputPath(String outputPath) {
            if (outputPath == null)
                throw new IllegalArgumentException("argument null");
            svdCleanData.setOutputPath(outputPath);
            isSetOutputPath = true;
            return this;
        }
        
        public SvdCleanData create()
        {
            if (!isSetTurn)
                throw new IllegalStateException("Parameter Turn not set");
            if (!isSetP)
                throw new IllegalStateException("Parameter P not set");
            if (!isSetSumSquare)
                throw new IllegalStateException("Parameter SumSquare not set");
            if (!isSetSing_val)
                throw new IllegalStateException("Parameter Sing_val not set");
            if (!isSetFile)
                throw new IllegalStateException("Parameter File not set");
            if (!isSetOutputPath)
                throw new IllegalStateException("Parameter OutputPath not set");
            return svdCleanData;
        }
    }
}