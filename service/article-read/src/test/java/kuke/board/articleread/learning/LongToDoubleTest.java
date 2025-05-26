package kuke.board.articleread.learning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * long 타입과 double 타입 간의 변환 시 발생할 수 있는 정밀도 손실을 테스트하는 클래스
 * 이 테스트는 데이터 타입 변환 시 주의해야 할 점을 보여줍니다.
 */
public class LongToDoubleTest {
    
    /**
     * long에서 double로 변환 시 정밀도 손실을 확인하는 테스트 메소드
     * 
     * 이 테스트는 다음을 보여줍니다:
     * 1. long 값을 double로 변환할 때 정밀도 손실 가능성
     * 2. double을 다시 long으로 변환 시 원래 값과 다를 수 있음
     * 3. BigDecimal을 사용하여 실제 double 값 확인
     */
    @Test
    void longToDoubleTest() {
        // long은 64비트 정수형으로, -2^63 ~ 2^63-1 범위의 정수를 정확히 표현 가능
        // double은 64비트 부동소수점으로, 매우 큰 범위의 수를 표현할 수 있지만 정밀도에 한계가 있음
        
        // 1. 매우 큰 long 값 생성 (111조 이상의 값)
        long longValue = 111_111_111_111_111_111L;
        System.out.println("longValue = " + longValue); // 원본 long 값 출력
        
        // 2. long을 double로 변환
        // 이 과정에서 double의 부동소수점 표현 방식으로 인해 정밀도 손실이 발생할 수 있음
        double doubleValue = longValue;
        
        // 3. BigDecimal을 사용하여 double 값의 정확한 표현 확인
        // double을 바로 출력하면 지수 표기법으로 출력될 수 있어 실제 값을 파악하기 어려움
        System.out.println("doubleValue = " + new BigDecimal(doubleValue).toString());
        
        // 4. double을 다시 long으로 변환
        // 이미 double 변환 과정에서 정밀도가 손실되었다면, 원래의 long 값과 달라질 수 있음
        long longValue2 = (long) doubleValue;
        System.out.println("longValue2 = " + longValue2);
        
        // 실행 결과 예상:
        // longValue = 111111111111111111
        // doubleValue = 111111111111111104 (마지막 자리 정밀도 손실)
        // longValue2 = 111111111111111104 (원본과 다른 값)
    }
}