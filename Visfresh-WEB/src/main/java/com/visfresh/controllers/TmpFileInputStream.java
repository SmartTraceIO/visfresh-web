/**
 *
 */
package com.visfresh.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TmpFileInputStream extends FileInputStream {
    private volatile File file;

    /**
     * @param file tmp file
     * @throws FileNotFoundException
     */
    public TmpFileInputStream(final File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    /* (non-Javadoc)
     * @see java.io.FileInputStream#close()
     */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            final File f= this.file;
            file = null;

            if (f != null) {
                f.delete();
            }
        }
    }
    /* (non-Javadoc)
     * @see java.io.FileInputStream#finalize()
     */
    @Override
    protected void finalize() throws IOException {
        try {
            super.finalize();
        } finally {
            try {
                close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
