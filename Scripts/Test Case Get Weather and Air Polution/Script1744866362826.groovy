import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.checkpoint.Checkpoint as Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testcase.TestCase as TestCase
import com.kms.katalon.core.testdata.TestData as TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject as TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import internal.GlobalVariable as GlobalVariable
import org.openqa.selenium.Keys as Keys
import groovy.json.JsonSlurper
import com.kms.katalon.core.util.KeywordUtil

import groovy.json.JsonSlurper
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.util.KeywordUtil

// API Get Geocode
def geoResp = WS.sendRequest(findTestObject('Object Repository/Get Weather Geocoding'))
WS.verifyResponseStatusCode(geoResp, 200)

def geoJson = new JsonSlurper().parseText(geoResp.getResponseText())

assert geoJson[0].name.toLowerCase().contains("south jakarta")

def lat = geoJson[0].lat
def lon = geoJson[0].lon

KeywordUtil.logInfo("Latitude: ${lat}")
KeywordUtil.logInfo("Longitude: ${lon}")

// JSON Schema untuk Geocode sebagai String
def geoJsonSchema = '''
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "name": { "type": "string" },
      "local_names": {
        "type": "object",
        "properties": {
          "en": { "type": "string" },
          "id": { "type": "string" }
        },
        "required": ["en", "id"]
      },
      "lat": { "type": "number" },
      "lon": { "type": "number" },
      "country": { "type": "string" }
    },
    "required": ["name", "local_names", "lat", "lon", "country"]
  }
}
'''

// Validasi JSON Schema untuk Geocode
WS.validateJsonAgainstSchema(geoResp, geoJsonSchema)
KeywordUtil.logInfo("Geocoding Validated")

// API Get Weather
def weatherResp = WS.sendRequest(findTestObject('Object Repository/Get Weather', [
	'lat': lat,
	'lon': lon,
	'API_KEY': GlobalVariable.API_KEY
]))
WS.verifyResponseStatusCode(weatherResp, 200)

def weatherJson = new JsonSlurper().parseText(weatherResp.getResponseText())

assert weatherJson.city.country == "ID"

// JSON Schema untuk Weather sebagai String
def weatherJsonSchema = '''
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "city": {
      "type": "object",
      "properties": {
        "country": { "type": "string" },
        "name": { "type": "string" }
      },
      "required": ["country", "name"]
    },
    "list": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "main": {
            "type": "object",
            "properties": {
              "temp": { "type": "number" },
              "pressure": { "type": "number" }
            },
            "required": ["temp", "pressure"]
          }
        }
      }
    }
  }
}
'''

// Validasi JSON Schema untuk Weather
WS.validateJsonAgainstSchema(weatherResp, weatherJsonSchema)
KeywordUtil.logInfo("Weather Forecast Validated")

// API Get Air Pollution
def airResp = WS.sendRequest(findTestObject('Object Repository/Get Air Pollution', [
	'lat': lat,
	'lon': lon,
	'API_KEY': GlobalVariable.API_KEY
]))
WS.verifyResponseStatusCode(airResp, 200)

def airJson = new JsonSlurper().parseText(airResp.getResponseText())

assert airJson.list.size() > 0
assert airJson.list[0].main.aqi != null

// JSON Schema untuk Air Pollution sebagai String
def airPollutionJsonSchema = '''
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "list": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "main": {
            "type": "object",
            "properties": {
              "aqi": { "type": "integer" }
            },
            "required": ["aqi"]
          }
        }
      }
    }
  }
}
'''

// Validasi JSON Schema untuk Air Pollution
WS.validateJsonAgainstSchema(airResp, airPollutionJsonSchema)
KeywordUtil.logInfo("Air Pollution Data Verified")

