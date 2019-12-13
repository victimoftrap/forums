// REVU я бы разбил на 2 подпакета - requests и responses
// или наоборот
// net.thumbtack.forums.dto.requests и net.thumbtack.forums.dto.responses, а в них уже подпакеты message, user...
// вот хочу сейчас реквесты Ваши посмотреть на предмет валидации, а тут респонсы мешаются
package net.thumbtack.forums.dto.message;

import javax.validation.constraints.NotBlank;

public class CreateCommentDtoRequest {
    @NotBlank
    private String body;

    public CreateCommentDtoRequest(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
