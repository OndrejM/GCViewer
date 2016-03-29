package com.tagtraum.perf.gcviewer.exp.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.exp.AbstractDataWriter;
import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Write GC history with comma separated values.
 * <p>
 * It uses the {@literal "Timestamp(sec/#),Used(K),Total(K),Pause(sec),GC-Type"} format.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 */
public class CSVDataWriter extends AbstractDataWriter {

    public CSVDataWriter(OutputStream out) {
        super(out);
    }

    private void writeHeader() {
        out.println("DateTime,Timestamp(sec/#),Used(K),Total(K),Pause(sec),GC-Type");
    }

    /**
     * Writes the model and flushes the internal PrintWriter.
     */
    public void write(GCModel model) throws IOException {
        writeHeader();

        Iterator<GCEvent> i = model.getGCEvents();
        long suggestedStartDate = model.getLastModified();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        if (model.hasDateStamp()) {
            suggestedStartDate = model.getFirstDateStamp().toInstant().toEpochMilli();
        } else if (model.hasCorrectTimestamp()) {
            suggestedStartDate -= (long)(model.getRunningTime() * 1000.0d);
        }
        while (i.hasNext()) {
            GCEvent event = i.next();
            // write always two lines so that there is a nice used memory curve
            double eventStartTimestamp = event.getTimestamp();
            if (model.hasCorrectTimestamp()) {
                // we have the timestamps therefore we can correct it with the pause time
                eventStartTimestamp = (event.getTimestamp() - event.getPause());
            }
            out.print(eventStartTimestamp);
            out.print(',');
            out.print(dateFormat.format(new Date((long)(suggestedStartDate + (eventStartTimestamp* 1000.0d)))));
            out.print(',');
            out.print(event.getPreUsed()); // pre
            out.print(',');
            out.print(event.getTotal());
            out.print(',');
            out.print(event.getPause());
            out.print(',');
            out.println(event.getExtendedType());

            out.print(event.getTimestamp());
            out.print(',');
            out.print(dateFormat.format(new Date((long)(suggestedStartDate + (event.getTimestamp()* 1000.0d)))));
            out.print(',');
            out.print(event.getPostUsed()); // post
            out.print(',');
            out.print(event.getTotal());
            out.print(',');
            out.print(0);
            out.print(',');
            out.println("NONE");
        }

        out.flush();
    }

}
