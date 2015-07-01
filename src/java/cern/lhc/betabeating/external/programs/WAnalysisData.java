package cern.lhc.betabeating.external.programs;

import java.util.List;

import cern.lhc.betabeating.external.interfaces.ProgramData;

public class WAnalysisData implements ProgramData{
    private String twissPath;
    private String outputPath;
    private String algorithm;
    private String accelerator;
    private String qx;
    private String qy;
    private String qdx;
    private String qdy;
    private String data;
    //data missing
    
    private WAnalysisData() {
    }
    
    @Override
    public String getArguments() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" --twiss=\"").append(twissPath).append("\"")
                     .append(" --output=\"").append(outputPath).append("\"")
                     .append(" --algorithm=").append(algorithm)
                     .append(" --accel=").append(accelerator)
                     .append(" --qx=").append(qx)
                     .append(" --qy=").append(qy)
                     .append(" --qdx=").append(qdx)
                     .append(" --qdy=").append(qdy)
                     .append(" ").append(data);
        return stringBuilder.toString();
    }
    
    public static WAnalysisDataCreate prepareObject()
    {
        return new WAnalysisDataCreate(new WAnalysisData());
    }
    
    private void setTwissPath(String twissPath) {
        this.twissPath = twissPath;
    }

    private void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    private void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    private void setAccelerator(String accelerator) {
        this.accelerator = accelerator;
    }

    private void setQx(String qx) {
        this.qx = qx;
    }

    private void setQy(String qy) {
        this.qy = qy;
    }

    private void setQdx(String qdx) {
        this.qdx = qdx;
    }

    private void setQdy(String qdy) {
        this.qdy = qdy;
    }
    
    private void setData(String data) {
        this.data = data;
    }
    
    public String getTwissPath() {
        return twissPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getAccelerator() {
        return accelerator;
    }

    public String getQx() {
        return qx;
    }

    public String getQy() {
        return qy;
    }

    public String getQdx() {
        return qdx;
    }

    public String getQdy() {
        return qdy;
    }
    
    public String getData() {
        return data;
    }

    public static class WAnalysisDataCreate
    {
        private WAnalysisData wAnalysisData;
        private boolean isSetTwissPath;
        private boolean isSetOutputPath;
        private boolean isSetAlgorithm;
        private boolean isSetAccelerator;
        private boolean isSetQx;
        private boolean isSetQy;
        private boolean isSetQdx;
        private boolean isSetQdy;
        private boolean isSetData;
        
        private WAnalysisDataCreate(WAnalysisData wAnalysisData) {
            this.wAnalysisData = wAnalysisData;
        }
        
        public WAnalysisDataCreate setTwissPath(String twissPath) {
            wAnalysisData.setTwissPath(twissPath);
            isSetTwissPath = true;
            return this;
        }
        
        public WAnalysisDataCreate setOutputPath(String outputPath) {
            wAnalysisData.setOutputPath(outputPath);
            isSetOutputPath = true;
            return this;
        }

        public WAnalysisDataCreate setAlgorithm(String algorithm) {
            wAnalysisData.setAlgorithm(algorithm);
            isSetAlgorithm = true;
            return this;
        }

        public WAnalysisDataCreate setAccelerator(String accelerator) {
            wAnalysisData.setAccelerator(accelerator);
            isSetAccelerator = true;
            return this;
        }

        public WAnalysisDataCreate setQx(String qx) {
            wAnalysisData.setQx(qx);
            isSetQx = true;
            return this;
        }

        public WAnalysisDataCreate setQy(String qy) {
            wAnalysisData.setQy(qy);
            isSetQy = true;
            return this;
        }

        public WAnalysisDataCreate setQdx(String qdx) {
            wAnalysisData.setQdx(qdx);
            isSetQdx = true;
            return this;
        }

        public WAnalysisDataCreate setQdy(String qdy) {
            wAnalysisData.setQdy(qdy);
            isSetQdy = true;
            return this;
        }
        
        public WAnalysisDataCreate setData(String data) {
            wAnalysisData.setData(data);
            isSetData = true;
            return this;
        }
        
        public WAnalysisDataCreate setData(List<CharSequence> data) {
            StringBuilder stringBuilder = new StringBuilder();
            for (CharSequence dataItem : data) 
                stringBuilder.append(dataItem);
            wAnalysisData.setData(stringBuilder.toString());
            isSetData = true;
            return this;
        }
        
        public WAnalysisData create()
        {
            if (!isSetTwissPath)
                throw new IllegalStateException("Parameter TwissPath not set");
            if (!isSetOutputPath)
                throw new IllegalStateException("Parameter OutputPath not set");
            if (!isSetAccelerator)
                throw new IllegalStateException("Parameter Accelerator not set");
            if (!isSetAlgorithm)
                throw new IllegalStateException("Parameter Algorithm not set");
            if (!isSetQdx)
                throw new IllegalStateException("Parameter qdx not set");
            if (!isSetQdy)
                throw new IllegalStateException("Parameter qdy not set");
            if (!isSetQx)
                throw new IllegalStateException("Parameter qx not set");
            if (!isSetQy)
                throw new IllegalStateException("Parameter qy not set");
            if (!isSetData)
                throw new IllegalStateException("Data not set");
            return wAnalysisData;
        }
    }
}

/*

example:

export PYTHONPATH=$PYTHONPATH:/afs/cern.ch/eng/sl/lintrack/Python_Classes4MAD/
/usr/bin/python /afs/cern.ch/eng/sl/lintrack/Beta-Beat.src/GetLLM/getsuper.py \
 --twiss="/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/models/LHCB1/beam1_dry_run_1_10_1_3_try2/" \
 --output=output -t SUSSIX -a LHCB1 \
 --beam=B1 --qx=0.31 --qy=0.32 \
 --qdx=0.304 --qdy=0.326 \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_14_23_920//Beam1@Turn@2011_08_24@09_14_23_920_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@08_20_00_086//Beam1@Turn@2011_08_24@08_20_00_086_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@08_21_27_607//Beam1@Turn@2011_08_24@08_21_27_607_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_40_52_536//Beam1@Turn@2011_08_24@09_40_52_536_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_13_01_678//Beam1@Turn@2011_08_24@09_13_01_678_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_39_33_625//Beam1@Turn@2011_08_24@09_39_33_625_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_38_13_893//Beam1@Turn@2011_08_24@09_38_13_893_0.sdds.new" \
 "/user/slops/data/LHC_DATA/OP_DATA/Betabeat/11-1-2012/LHCB1/Measurements/Beam1@Turn@2011_08_24@09_11_30_026//Beam1@Turn@2011_08_24@09_11_30_026_0.sdds.new"
 
 
 Options:
  -h, --help            show this help message and exit
  -m twiss, --twiss=twiss
                        twiss files to use
  -o <path>, --output=<path>
                        output path, where to store the results
  -b <path>, --beta=<path>
                        where beta-beat is stored
  -t ALGORITHM, --algorithm=ALGORITHM
                        Which technique to use (SUSSIX/SVD)
  -a ACCEL, --accel=ACCEL
                        Which accelerator: LHCB1 LHCB2 SPS RHIC
  --qx=<value>          Fractional horizontal tune
  --qy=<value>          Fractional vertical tune
  --qdx=<value>         AC dipole driven horizontal tune
  --qdy=<value>         AC dipole driven vertical tune

 
 */