package com.gitthub.youssefagagg.ecommerceorderprocessor.util;

import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.ErrorCode;
import com.gitthub.youssefagagg.ecommerceorderprocessor.exception.custom.CustomException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KeyLockManager {

  private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();


  /**
   * Runs an action under a key-based lock and returns a value.
   */
  public <T> T withLock(String key, Supplier<T> action) {
    ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());
    boolean locked = lock.tryLock();
    if (!locked) {
      throw new CustomException(ErrorCode.GLOBAL_ERROR,
                                " Lock is already acquired for key: " + key);
    }

    try {
      return action.get();
    } finally {
      lock.unlock();
      log.info("Released lock for key: {}", key);
      if (!lock.isLocked()) {
        lockMap.remove(key, lock);
      }
    }
  }

}
