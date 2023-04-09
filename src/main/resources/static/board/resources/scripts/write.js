const form = window.document.getElementById('form');





let editor;
ClassicEditor
    .create(form['content'], {
        simpleUpload : { //이미지 넣기 위함
            uploadUrl : './image'
        }
    })
    .then(e => editor = e);
// 이 안에 있는 함수를 실행한다. 변수 e가 Editor 전체를 뜻함

form['back'].addEventListener('click', () => window.history.length < 2 ? window.close() : window.history.back());

form.onsubmit = (e) => {
    e.preventDefault();

    if (form['title'].value === '') {
        alert('제목을 입력해 주세요.');
        form['title'].focus();
        return;
    }
    if (editor.getData() === '') {
        alert('내용을 입력해 주세요.');
        editor.focus();
        return;
    }
    // Cover.show('게시글을 작성하고 있어요. \n 잠시만 기다려주세요.');
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('title', form['title'].value);
    formData.append('content', editor.getData());
    formData.append('bid', form['bid'].value);
    xhr.open('POST', './write');
    xhr.onreadystatechange = () => {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            // Cover.hide();
            if (xhr.status >= 200 && xhr.status < 300) {
                const responseObject = JSON.parse(xhr.responseText);
                switch (responseObject['result']) {
                    case 'success':
                        alert('게시글 입력 성공!');
                        // window.location.href = './read?aid=' + responseObject['aid']; // 인덱스번호 주소창에 나타내기 내꺼

                        //인덱스번호 주소창에 나타내기 쌤 꺼
                        const aid = responseObject['aid'];
                        window.location.href=`read?aid=${aid}`;
                        break;
                    case 'not_allowed':
                        alert('로그인 후 이용해주세요.');
                        break;


                    default:
                        alert('올바른 제목과 내용을 입력 후 다시 시도해주세요.');
                        break;
                }
                //응답코드 출력
            } else {//200-299 아니면
                alert('서버와 통신하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
            }
        }
    };
    xhr.send(formData);
}
// //formData.append는 html에 있는 값만
//
//
//
//
