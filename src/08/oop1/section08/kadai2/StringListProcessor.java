package oop1.section08.kadai2;

import java.util.List;

/**
 * 文字列リストのフォーマットを行うインターフェース
 */
public interface StringListProcessor {
    /**
     * 指定された文字列のリスト内の全ての要素を連結し、大文字に変換して返します。
     * - リストがnullの場合は、InvalidCollectionDataExceptionをスローします。
     * - リストが空の場合は、EmptyCollectionExceptionをスローします。
     * - リスト内にnull要素が含まれている場合は、NullItemInCollectionExceptionをスローします。
     * @param texts 文字列のリスト
     * @return 全ての文字列を連結し大文字に変換したもの
     * @throws InvalidCollectionDataException リストがnullの場合
     * @throws EmptyCollectionException リストが空の場合
     * @throws NullItemInCollectionException リスト内にnull要素が含まれる場合
     */
    String concatenateAndUppercase(List<String> texts) throws InvalidCollectionDataException, EmptyCollectionException, NullItemInCollectionException;
}