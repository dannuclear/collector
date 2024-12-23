package ru.bisoft.collector.batch;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.lang.Nullable;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MultiJdbcItemReader<T> implements ItemReader<T> {
    private static final Log logger = LogFactory.getLog(MultiJdbcItemReader.class);

    private DataSource[] dataSources;

    private JdbcCursorItemReader<? extends T> delegate;

    private int currentResource = -1;

    /**
     * Reads the next item, jumping to next resource if necessary.
     */
    @Nullable
    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException {

        if (dataSources == null || dataSources.length == 0) {
            return null;
        }

        // If there is no resource, then this is the first item, set the current
        // resource to 0 and open the first delegate.
        if (currentResource == -1) {
            currentResource = 0;
            delegate.setDataSource(dataSources[currentResource]);
            // delegate.open(new ExecutionContext());
        }

        return readNextItem();
    }

    /**
     * Use the delegate to read the next item, jump to next resource if current one
     * is
     * exhausted. Items are appended to the buffer.
     * 
     * @return next item from input
     */
    private T readNextItem() throws Exception {

        T item = delegate.read();

        while (item == null) {

            currentResource++;

            if (currentResource >= dataSources.length) {
                return null;
            }

            delegate.close();
            delegate.setDataSource(dataSources[currentResource]);
            delegate.open(new ExecutionContext());

            item = delegate.read();
        }

        return item;
    }

    public void setDelegate(JdbcCursorItemReader<? extends T> delegate) {
		this.delegate = delegate;
	}
}
