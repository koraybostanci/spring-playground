package dev.coding.service.httpbin;

public interface HttpBinService {
    String get();
    String post(String data);
    String put(String data);
}
