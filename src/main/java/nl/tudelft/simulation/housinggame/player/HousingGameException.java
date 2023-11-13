package nl.tudelft.simulation.housinggame.player;

/**
 * HousingGameException.java.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class HousingGameException extends Exception
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public HousingGameException()
    {
    }

    /**
     * @param message
     */
    public HousingGameException(final String message)
    {
        super(message);
    }

    /**
     * @param cause
     */
    public HousingGameException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public HousingGameException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public HousingGameException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
