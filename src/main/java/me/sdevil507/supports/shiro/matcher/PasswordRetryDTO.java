package me.sdevil507.supports.shiro.matcher;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 密码重试次数实体
 *
 * @author sdevil507
 */
public class PasswordRetryDTO implements Serializable {

    /**
     * 密码重试次数
     */
    private Integer count;

    /**
     * 记录时间
     */
    private LocalDateTime dateTime;

    public PasswordRetryDTO(Integer count, LocalDateTime dateTime) {
        this.count = count;
        this.dateTime = dateTime;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "PasswordRetryDTO{" +
                "count=" + count +
                ", dateTime=" + dateTime +
                '}';
    }
}
