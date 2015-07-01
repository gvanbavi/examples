/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN, All Rights Reserved.
 */
package cern.lhc.betabeating.model;

import java.io.File;

import cern.lhc.betabeating.Tools.FileIO;

public enum SddsFileType {
    BINARY, ASCII, NONE;

    public static SddsFileType getTypeFromFile(File file)
    {
        if (FileIO.fileContentStartsWith(file, "SDDS"))
            return BINARY;
        else if (FileIO.fileContentStartsWith(file, "#"))
            return ASCII;
        else
            return NONE;
    }
}