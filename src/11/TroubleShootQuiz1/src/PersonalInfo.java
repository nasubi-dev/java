package TroubleShootQuiz1.src;
public record PersonalInfo (

        String name,
        String nameKana,
        String mailAddress,
        String zipCode,
        String address1,
        String address2,
        String address3,
        String address4,
        String address5,
        int age,
        String bloodType
){
    public String toCSVRow() {
        return String.join(",",
                name + "(" + nameKana + ")",
                "〒" + zipCode + " " + address1 + address2 + address3 + address4 + address5,
                String.valueOf(age),
                bloodType
        );
    }
}
