package oop1.section08.kadai2;

import java.util.List;

/**
 * 数値リストの分析を行うインターフェース
 */
public interface NumberListAnalyzer {
    /**
     * 指定された整数のリストから最大値を見つけて返します。
     * - リストがnullの場合は、InvalidCollectionDataExceptionをスローします。
     * - リストが空の場合は、EmptyCollectionExceptionをスローします。
     * - リスト内にnull要素が含まれている場合は、NullItemInCollectionExceptionをスローします。
     * @param numbers 整数のリスト
     * @return リスト内の最大値
     * @throws InvalidCollectionDataException リストがnullの場合
     * @throws EmptyCollectionException リストが空の場合
     * @throws NullItemInCollectionException リスト内にnull要素が含まれる場合
     */
    int findMaximumValue(List<Integer> numbers) throws InvalidCollectionDataException, EmptyCollectionException, NullItemInCollectionException;
}