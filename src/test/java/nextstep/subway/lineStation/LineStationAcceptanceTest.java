package nextstep.subway.lineStation;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.dto.LineRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 구간 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LineStationAcceptanceTest {
    /**
     * Given 역 2개와 노선을 생성하고
     * When 사이에 새로운 역을 등록하면
     * Then 노선 조회 시 3개 역이 조회된다.
     */
    @DisplayName("역 사이에 새로운 역을 등록한다.")
    @Test
    void 역_사이에_새로운_역_등록() {
        // given
        JsonPath 강남역 = 지하철_역_생성("강남역");
        JsonPath 선릉역 = 지하철_역_생성("선릉역");
        JsonPath 이호선 = 지하철_노선_생성("2호선", "green", 강남역.getLong("id"), 선릉역.getLong("id"), 10);

        // when
        JsonPath 역삼역 = 지하철_역_생성("역삼역");
        Map paramMap = new HashMap();
        paramMap.put("upStationId", 강남역.getLong("id"));
        paramMap.put("downStationId", 역삼역.getLong("id"));
        paramMap.put("distance", 5);
        ExtractableResponse<Response> postResponse = 지하철_구간_추가(이호선.getLong("id"), paramMap);

        // then
        ExtractableResponse<Response> getResponse = 노선_아이디로_지하철역_조회(이호선.getLong("id"));
        assertThat(postResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(getResponse.jsonPath().getList("lineStations")).hasSize(3);
    }

    /**
     * Given 역 2개와 노선을 생성하고
     * When 새로운 역을 상행 종점으로 등록하면
     * Then 노선 조회 시 첫번째 역이 새로운 역과 일치한다.
     */
    @DisplayName("새로운 역을 상행 종점으로 등록한다.")
    @Test
    void 새로운_역을_상행_종점으로_등록() {
        // given
        JsonPath 강남역 = 지하철_역_생성("강남역");
        JsonPath 선릉역 = 지하철_역_생성("선릉역");
        JsonPath 이호선 = 지하철_노선_생성("2호선", "green", 강남역.getLong("id"), 선릉역.getLong("id"), 10);

        // when
        JsonPath 교대역 = 지하철_역_생성("교대역");
        Map paramMap = new HashMap();
        paramMap.put("upStationId", 교대역.getLong("id"));
        paramMap.put("downStationId", 강남역.getLong("id"));
        paramMap.put("distance", 5);
        ExtractableResponse<Response> postResponse = 지하철_구간_추가(이호선.getLong("id"), paramMap);

        // then
        ExtractableResponse<Response> getResponse = 노선_아이디로_지하철역_조회(이호선.getLong("id"));
        assertThat(postResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(getResponse.jsonPath().getList("lineStations")).hasSize(3);
        assertThat(com.jayway.jsonpath.JsonPath.parse(getResponse.jsonPath()).read("$.name[0]").equals("교대역")).isTrue();
    }

    /**
     * Given 역 2개와 노선을 생성하고
     * When 새로운 역을 하행 종점으로 등록하면
     * Then 노선 조회 시 마지막 역이 새로운 역과 일치한다.
     */
    @DisplayName("새로운 역을 하행 종점으로 등록한다.")
    @Test
    void 새로운_역을_하행_종점으로_등록() {
        // given
        JsonPath 강남역 = 지하철_역_생성("강남역");
        JsonPath 선릉역 = 지하철_역_생성("선릉역");
        JsonPath 이호선 = 지하철_노선_생성("2호선", "green", 강남역.getLong("id"), 선릉역.getLong("id"), 10);

        // when
        JsonPath 삼성역 = 지하철_역_생성("삼성역");
        Map paramMap = new HashMap();
        paramMap.put("upStationId", 선릉역.getLong("id"));
        paramMap.put("downStationId", 삼성역.getLong("id"));
        paramMap.put("distance", 5);
        ExtractableResponse<Response> postResponse = 지하철_구간_추가(이호선.getLong("id"), paramMap);

        // then
        ExtractableResponse<Response> getResponse = 노선_아이디로_지하철역_조회(이호선.getLong("id"));
        assertThat(postResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(getResponse.jsonPath().getList("lineStations")).hasSize(3);
        assertThat(com.jayway.jsonpath.JsonPath.parse(getResponse.jsonPath()).read("$.name[2]").equals("삼성역")).isTrue();
    }

    /**
     * Given 역 2개와 노선을 생성하고
     * When 사이에 기존 역 사이 길이보다 크거나 같은 역을 등록하면
     * Then 등록이 안된다.
     */
    @DisplayName("기존 역 사이 길이보다 크거나 같은 역을 등록한다.")
    @Test
    void 기존_역_사이와_같거나_긴_역_등록() {
        // given
        JsonPath 강남역 = 지하철_역_생성("강남역");
        JsonPath 선릉역 = 지하철_역_생성("선릉역");
        JsonPath 이호선 = 지하철_노선_생성("2호선", "green", 강남역.getLong("id"), 선릉역.getLong("id"), 10);

        // when
        JsonPath 삼성역 = 지하철_역_생성("길이가같은역");
        Map paramMap = new HashMap();
        paramMap.put("upStationId", 선릉역.getLong("id"));
        paramMap.put("downStationId", 삼성역.getLong("id"));
        paramMap.put("distance", 10);
        ExtractableResponse<Response> postResponse = 지하철_구간_추가(이호선.getLong("id"), paramMap);

        // then
        ExtractableResponse<Response> getResponse = 노선_아이디로_지하철역_조회(이호선.getLong("id"));
        assertThat(postResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(getResponse.jsonPath().getList("lineStations")).hasSize(2);
    }

    /**
     * Given 역 2개와 노선을 생성하고
     * When 생성한 노선을 다시 노선에 등록하면
     * Then 등록이 안된다.
     */
    @DisplayName("이미 등록된 상행역/하행역을 등록한다.")
    @Test
    void 이미_등록된_상행역_하행역을_등록() {
        // given
        JsonPath 강남역 = 지하철_역_생성("강남역");
        JsonPath 선릉역 = 지하철_역_생성("선릉역");
        JsonPath 이호선 = 지하철_노선_생성("2호선", "green", 강남역.getLong("id"), 선릉역.getLong("id"), 10);

        // when
        Map paramMap = new HashMap();
        paramMap.put("upStationId", 강남역.getLong("id"));
        paramMap.put("downStationId", 선릉역.getLong("id"));
        paramMap.put("distance", 5);
        ExtractableResponse<Response> postResponse = 지하철_구간_추가(이호선.getLong("id"), paramMap);

        // then
        ExtractableResponse<Response> getResponse = 노선_아이디로_지하철역_조회(이호선.getLong("id"));
        assertThat(postResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(getResponse.jsonPath().getList("lineStations")).hasSize(2);
    }

    /**
     * When 상행역 또는 하행역이 포함되지 않는 구간으로 등록하면
     * Then 등록이 안된다.
     */
    @DisplayName("상행역/하행역이 포함되지 않는 구간으로 등록한다.")
    @Test
    void 상행역_하행역이_포함되지_않는_구간으로_등록() {
        // given
        JsonPath 강남역 = 지하철_역_생성("강남역");
        JsonPath 선릉역 = 지하철_역_생성("선릉역");
        JsonPath 이호선 = 지하철_노선_생성("2호선", "green", 강남역.getLong("id"), 선릉역.getLong("id"), 10);

        // when
        JsonPath 신도림역 = 지하철_역_생성("신도림역");
        JsonPath 대림역 = 지하철_역_생성("대림역");
        Map paramMap = new HashMap();
        paramMap.put("upStationId", 신도림역.getLong("id"));
        paramMap.put("downStationId", 대림역.getLong("id"));
        paramMap.put("distance", 5);
        ExtractableResponse<Response> postResponse = 지하철_구간_추가(이호선.getLong("id"), paramMap);

        // then
        ExtractableResponse<Response> getResponse = 노선_아이디로_지하철역_조회(이호선.getLong("id"));
        assertThat(postResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(getResponse.jsonPath().getList("lineStations")).hasSize(2);
    }

    private JsonPath 지하철_역_생성(String name) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);

        return RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/stations")
                .then().log().all()
                .extract()
                .jsonPath();
    }

    private JsonPath 지하철_노선_생성(String name, String color, Long upStationId,
                                                     Long downStationId, int distance) {
        LineRequest lineRequest = new LineRequest(name, color, upStationId, downStationId, distance);

        return RestAssured.given().log().all()
                .body(lineRequest)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract()
                .jsonPath();
    }

    private ExtractableResponse<Response> 지하철_구간_추가(Long lineId, Map paramMap) {
        return RestAssured.given().log().all()
                .body(paramMap)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/" + lineId + "/sections")
                .then().log().all()
                .extract();
    }

    private ExtractableResponse<Response> 노선_아이디로_지하철역_조회(Long lineId) {
        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/" + lineId + "/stations")
                .then().log().all()
                .extract();
    }
}