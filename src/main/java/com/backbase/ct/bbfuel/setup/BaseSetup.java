package com.backbase.ct.bbfuel.setup;

import com.backbase.ct.bbfuel.util.GlobalProperties;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseSetup {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected GlobalProperties globalProperties = GlobalProperties.getInstance();

    public abstract void initiate() throws IOException;
}
