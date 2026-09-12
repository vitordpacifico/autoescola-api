package com.fiap.autoescola.util;

public final class CpfValidator {
    private CpfValidator() {
    }

    public static boolean isValid(String cpf) {
        if (cpf == null) {
            return false;
        }
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11 || digits.chars().distinct().count() == 1) {
            return false;
        }

        int[] nums = digits.chars().map(c -> c - '0').toArray();

        int dv1 = calcularDigito(nums, 9, 10);
        if (dv1 != nums[9]) {
            return false;
        }

        int dv2 = calcularDigito(nums, 10, 11);
        return dv2 == nums[10];
    }

    private static int calcularDigito(int[] nums, int tamanho, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += nums[i] * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
