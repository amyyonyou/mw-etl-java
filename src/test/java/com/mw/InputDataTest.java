package com.mw;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mw.etl.ctdb.rtdb.inputdata.InputDataService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InputDataTest {
private static final Logger logger = LoggerFactory.getLogger(InputDataTest.class);
	
    @Autowired
	private InputDataService inputDataService;
    
    @Test
    public void test() throws Exception {
		inputDataService.saveData();
	}
    
}

