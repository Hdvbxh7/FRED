import java.io.File;

import java.io.FileFilter;

public class DossFilter implements FileFilter{

    String dossName;

    DossFilter(String name){
        dossName = name;
    }

    /**
     * Tests whether or not the specified abstract pathname should be
     * included in a pathname list.
     *
     * @param  pathname  The abstract pathname to be tested
     * @return  {@code true} if and only if {@code pathname}
     *          should be included
     */
    public boolean accept(File pathname){
        return pathname.isDirectory() && (pathname.getName().equals(dossName));
    }
}

