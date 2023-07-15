package br.unb.sds.gds2ephem.ephem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EphemUtilsTest {
    @Test
    void converterPythonStringTupleEmList() {
        final var customTuples = EphemUtils.converterPythonStringTupleEmList("[('leadership', 'Level 5: Leadership'), ('pheoc', 'Level 4: PHEOC'), ('ministries', 'Level 3: Ministries'), ('partners', 'Level 2: Partners'), ('everyone', 'Level 1: Everyone')]");

        assertEquals("[EphemUtils.CustomTuple(key=leadership, value=Level 5: Leadership'), EphemUtils.CustomTuple(key='pheoc, value=Level 4: PHEOC'), EphemUtils.CustomTuple(key='ministries, value=Level 3: Ministries'), EphemUtils.CustomTuple(key='partners, value=Level 2: Partners'), EphemUtils.CustomTuple(key='everyone, value=Level 1: Everyone)]", customTuples.toString());
    }
}