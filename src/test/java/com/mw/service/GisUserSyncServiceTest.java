/**
 * 
 */
package com.mw.service;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mw.gis.user.GisUserSyncService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GisUserSyncServiceTest {
	private static final Logger logger = LoggerFactory.getLogger(GisUserSyncServiceTest.class);
	
    @Autowired
	private GisUserSyncService gisUserSyncService;
    
    @Test
	public void sync() throws IOException{
    	gisUserSyncService.sync();
	}
    

}
