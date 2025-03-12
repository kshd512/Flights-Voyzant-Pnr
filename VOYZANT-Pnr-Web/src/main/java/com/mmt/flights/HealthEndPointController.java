package com.mmt.flights;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.mmt.flights.constants.EndpointConstants.HEALTH_END_POINT;

/**
 * Health end point controller
 *
 */
@RestController
public class HealthEndPointController {

	/**
	 * @return server health with status as up
	 */
	@ApiOperation(value = "return server health for dependent API's", response = String.class, notes = "generate server heath for all the dependent  API..")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "server heath is UP") })
	@GetMapping(value = HEALTH_END_POINT, produces = { "application/json" })
	public ResponseEntity<?> tphealth() {
		// implement your down stream api health check here example: if using
		// couchbase check its connectivity and return status of all dependent
		// third party health.
		return new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK);
	}
	
	@ApiOperation(value = "return server health for API", response = String.class, notes = "generate server health for API..")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "server heath is UP") })
    @GetMapping(value = "/1", produces = { "application/json" })
    public ResponseEntity<?> health() {
        return new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK);
    }

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ResponseEntity<String> welcome() {
		return new ResponseEntity<>("<h1><b>VOYZANT PNR is up and running &#9786</b></h1>", HttpStatus.OK);
	}
}
