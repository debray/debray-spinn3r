package com.spinn3r.api;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Date;


import org.w3c.dom.Document;

public abstract class LegacyWrapperClient <ResultType extends BaseResult> extends BaseClient<ResultType> {

    private static int PARALLELISM        = 4;
    private static int RESULT_BUFFER_SIZE = 8;

    protected Config                       config        = null;
    private   BaseClientResult<ResultType> result        = null;
    private   long                         sleepDuration = 0;

    private   ParallelFetchHelper<ResultType> parallelFetcher = null;

    public LegacyWrapperClient () {
        result = new BaseClientResult<ResultType> ( null );
    }


    public void fetch() throws IOException,
                               ParseException,
                               InterruptedException {


        if ( parallelFetcher == null ) {
            parallelFetcher = new ParallelFetchHelper<ResultType> ( this, config, RESULT_BUFFER_SIZE, PARALLELISM );
            parallelFetcher.start();

        }

        long sleep_interval = 0;

        // we don't acutally sleep hear as we allready sleeped in the parallelFetcher.
        if ( result != null && ! result.getHasMoreResults() )
            sleep_interval = result.getConfig().getSleepInterval();

        setSleepDuration( sleep_interval );

        try {
            result = parallelFetcher.fetch();

            config.setNextRequestURL( result.getNextRequestURL() );
        }

        catch ( Exception e ) {
            // we do this cause it the caller retries we need a clean pipe line
            parallelFetcher = null;

            //this is slightly ugly but prevents nested exceptions.
            if ( e instanceof IOException )
                throw (IOException)e;

            if ( e instanceof ParseException )
                throw (ParseException)e;

            if ( e instanceof InterruptedException )
                throw (InterruptedException)e;

            IOException ioe = new IOException();
            ioe.initCause( e );
            throw ioe;
        }

    }


    public Document doXmlFetch ( String resource, Config config ) throws IOException,
                                                      ParseException,
                                                      InterruptedException {
        return doXmlFetch( getConnection( resource ).getInputStream(), config );
    }
 
    // **** Getter and setters **************************************************



    /**
     * 
     * Get the value of <code>sleepDuration</code>.
     *
     */
    public long getSleepDuration() { 
        return sleepDuration;
    }

    /**
     * 
     * Set the value of <code>sleepDuration</code>.
     *
     */
    public void setSleepDuration( long value ) { 
        sleepDuration = value;
    }


    /**
     * 
     * Get the value of <code>config</code>.
     *
     */
    public Config getConfig() { 
        return this.config;
    }

    /**
     * 
     * Set the value of <code>config</code>.
     *
     */
    public void setConfig( Config config ) { 
        this.config = config;
    }

    // **** Proxy Getter and setters **************************************************

    /**
     * 
     * Get the last requested URL for debug and logging purposes.
     *
     */
    public String getLastRequestURL() { 
        return result.getLastRequestURL();
    }

    /**
     * 
     * Set the value of <code>lastRequestURL</code>.
     *
     */
    public void setLastRequestURL( String lastRequestURL ) { 
        result.setLastRequestURL( lastRequestURL );
    }

    /**
     * 
     * Get the value of <code>nextRequestURL</code>.
     *
     */
    public String getNextRequestURL() { 
        return result.getNextRequestURL();
    }

    /**
     * 
     * Set the value of <code>nextRequestURL</code>.
     *
     */
    public void setNextRequestURL( String next ) { 
        result.setNextRequestURL( next );
    }

     /**
      * 
      * Get the value of <code>result</code>.
      *
      */
     public List<ResultType> getResults() { 
         return result.getResults();
     }


    /**
     * 
     * Get the value of <code>callDuration</code>.
     *
     */
    public long getCallDuration() { 
        return result.getCallDuration();
    }

    /**
     * 
     * Set the value of <code>callDuration</code>.
     *
     */
    public void setCallDuration( long callDuration ) { 
        result.setCallDuration( callDuration );
    }


    /**
     * 
     * Get the value of <code>parseDuration</code>.
     *
     */
    public long getParseDuration() { 
        return result.getParseDuration();
    }

    /**
     * 
     * Set the value of <code>parseDuration</code>.
     *
     */
    public void setParseDuration( long parseDuration ) { 
        result.setParseDuration( parseDuration );
    }


    /**
     * When the API needs to shutdown you need to call this method FIRST and
     * persist it.  Then when the API starts you need to call config.setAfter()
     * with this value.
     */
    public Date getRestartPoint() {
        return result.getRestartPoint();
    }

    /**
     * Return the correct limit, factoring in the limit set by the user. 
     *
     */
    public int getLimit() {
        return result.getRequestLimit();
    }

    /**
     * Return true if more results are available.
     *
     */
    public boolean hasMoreResults() {
        return result.getHasMoreResults();
    }

    /**
     * Return a copy of the input streem
     */
    public InputStream getInputStream( ) throws IOException {
        return getInputStream( true );
    }


    /**
     * Return a copy of the input streem
     */
    public InputStream getInputStream( boolean decompress ) throws IOException {
        return result.getInputStream( decompress );
    }
       

    /**
     * Return if the results are compressed.
     */
    public boolean getIsCompressed () {
        return result.getIsCompressed();
    }
}
