const form = window.document.getElementById('form');



form.onsubmit=(e)=>{
    e.preventDefault();
    //자동으로 경고창이 안나오게 해주는거
    // Warning.hide();
    if (form['email'].value === '') {
        alert('이메일을 입력해주세요');
        form['email'].focus();
        return;
    }

    if (form['password'].value === '') {
        alert('비밀번호를 입력해주세요');
        form['password'].focus();
        return;
    }

    // Cover.show('로그인 하는 중입니다.\n 잠시만 기다려주세요. ');
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', form['email'].value);
    formData.append('password', form['password'].value);
    xhr.open('POST', './login');
    xhr.onreadystatechange = () => {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            // Cover.hide();
            if (xhr.status >= 200 && xhr.status < 300) {
                const responseObject = JSON.parse(xhr.responseText);
                //"{"result":"success"}"
                switch (responseObject['result']) {
                    case 'success':
                        window.location.href = '/board/list?bid=notice';
                        break;
                    default:
                        alert('알맞은 이메일과 비밀번호를 입력해주세요.')

                }
            } else {
                alert('서버와 통신하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
            }
        }
    };
    xhr.send(formData);
};