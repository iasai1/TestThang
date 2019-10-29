package com.diana.autotest;

import com.diana.config.PersistenceConfig;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;


import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.Thread.sleep;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceConfig.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
public class AutoTests {

    @Autowired
    private ApplicationContext applicationContext;

    private static final int PORT = 8953;

    private static final Logger LOG = LoggerFactory.getLogger(AutoTests.class);

    private WebDriver webDriver = null;

    @Before
    public void setUp() throws Exception {
        resetAutoIncrement(applicationContext, "employees", "departments");
        System.setProperty("webdriver.chrome.driver", "C:\\DevTools\\chromedriver.exe");
        webDriver = new ChromeDriver();
        webDriver.manage().window().setSize(new Dimension(800, 600));
    }

    @After
    public void tearDown() throws Exception{
        webDriver.quit();
    }

    @Test
    @DatabaseSetup("intTestDB.xml")
    public void testEmployeeRender(){
        webDriver.get("http://localhost:" + PORT +"/employees");

        Assert.assertNotSame(webDriver.findElements(By.className("btnInfo")).size(), 0);
        Assert.assertNotSame(webDriver.findElements(By.id("newEmp")).size(), 0);
        Assert.assertNotSame(webDriver.findElements(By.id("deps")).size(), 0);
    }

    @Test
    @DatabaseSetup("intTestDB.xml")
    @ExpectedDatabase("initTestDB-addDep-expected.xml")
    public void test_goToNewDep_thenCreateDep_thenGoBack() throws  Exception{

        webDriver.get("http://localhost:"+ PORT +"/");

        sleep(100);

        Assert.assertEquals("http://localhost:"+ PORT +"/employees", webDriver.getCurrentUrl());

        webDriver.findElement(By.id("deps")).click();

        sleep(100);

        Assert.assertEquals("http://localhost:"+ PORT +"/departments", webDriver.getCurrentUrl());

        webDriver.findElement(By.id("newDep")).click();

        sleep(100);

        Assert.assertEquals("http://localhost:" + PORT +"/newDepartment", webDriver.getCurrentUrl());

        Assert.assertNotSame(webDriver.findElements(By.id("name")).size(), 0);
        webDriver.findElement(By.id("name")).sendKeys("coast");

        Assert.assertNotSame(webDriver.findElements(By.id("add")).size(), 0);
        webDriver.findElement(By.id("add")).click();

        sleep(500);

        Assert.assertEquals("http://localhost:"+ PORT +"/departments", webDriver.getCurrentUrl());

    }

    @Test
    @DatabaseSetup("intTestDB.xml")
    @ExpectedDatabase("initTestDB-addEmp-expected.xml")
    public void test_goToNewEmp_thenCreateEmp_thenGoBack() throws  Exception{
        webDriver.get("http://localhost:"+ PORT +"/employees");

        Integer n = webDriver.findElements(By.className("btnInfo")).size();

        Assert.assertNotSame(n, 0);

        webDriver.findElement(By.id("newEmp")).click();

        sleep(100);

        Assert.assertEquals("http://localhost:"+ PORT +"/newEmployee", webDriver.getCurrentUrl());

        Assert.assertNotSame(webDriver.findElements(By.id("name")).size(), 0);
        webDriver.findElement(By.id("name")).sendKeys("Toosty");

        Assert.assertNotSame(webDriver.findElements(By.id("phone")).size(), 0);
        webDriver.findElement(By.id("phone")).sendKeys("1234");

        Assert.assertNotSame(webDriver.findElements(By.id("city")).size(), 0);
        webDriver.findElement(By.id("city")).sendKeys("asd");

        Assert.assertNotSame(webDriver.findElements(By.id("street")).size(), 0);
        webDriver.findElement(By.id("street")).sendKeys("ggg");

        Assert.assertNotSame(webDriver.findElements(By.id("depName")).size(), 0);
        Select depName = new Select(webDriver.findElement(By.id("depName")));
        depName.selectByVisibleText("coast");

        Assert.assertNotSame(webDriver.findElements(By.id("add")).size(), 0);
        webDriver.findElement(By.id("add")).click();

        sleep(500);

        Assert.assertEquals("http://localhost:"+ PORT +"/employees", webDriver.getCurrentUrl());

        Assert.assertEquals(webDriver.findElements(By.className("btnInfo")).size(), n + 1);

        webDriver.findElements(By.className("btnInfo")).get(1).click();
        sleep(1000);
        Alert alert = webDriver.switchTo().alert();
        System.out.println(alert.getText());
        alert.accept();
    }

    private void resetAutoIncrement(ApplicationContext applicationContext, String... tables) throws SQLException {
        DataSource dataSource = applicationContext.getBean(DataSource.class);
        String sqlTemplate = applicationContext.getBean(Environment.class).getRequiredProperty("test.reset.sql.template");
        try(Connection connection = dataSource.getConnection()){
            for(String table: tables){
                try(Statement statement = connection.createStatement()){
                    String resetSql = String.format(sqlTemplate, table);
                    statement.execute(resetSql);
                }
            }
        }
    }

}