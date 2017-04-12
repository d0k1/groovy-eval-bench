/**
 * Created by dkirpichenkov on 07.04.17.
 */

def testFunction(){
    def result = [];
    result.add(1.0);

    return result[0];
}

def testFunction2(){
    def result = [];
    result.add(1.0);

    return result[0]*100.0;
}

return testFunction() + testFunction2();