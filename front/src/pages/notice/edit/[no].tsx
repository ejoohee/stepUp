import React, {useState, useRef, useEffect} from "react";
import Header from "components/Header";
import MainBanner from "components/MainBanner";
import SubNav from "components/subNav";
import Footer from "components/Footer";

import { axiosBoard, axiosUser } from "apis/axios";

import { accessTokenState, refreshTokenState, idState, nicknameState } from "states/states";
import { useRecoilState } from "recoil";
import { useRouter } from "next/router";

const NoticeCreate = () => {
    const noticeTitle = useRef<any>();
    const noticeContent = useRef<any>();
    const noticeFile = useRef<any>();
    const router = useRouter();
    const boardId = router.query.no;

    const [accessToken, setAccessToken] = useRecoilState(accessTokenState);
    const [refreshToken, setRefreshToken] = useRecoilState(refreshTokenState);
    const [id, setId] = useRecoilState(idState);
    const [nickname, setNickaname] = useRecoilState(nicknameState);
    const [notice, setNotice] = useState<any>();

    const editNotice = async (e: any) => {
        e.preventDefault();

        await axiosBoard.put("/notice", {
            boardId: boardId,
            title: noticeTitle.current?.value,
            content: noticeContent.current?.value,
            writerName: notice.writerName,
            writerProfileImg: notice.writerProfileImg,
            fileURL: noticeFile.current?.value,
            boardType: notice.boardType,
            randomDanceId: 1,
        },{
            headers:{
                Authorization: `Bearer ${accessToken}`,
            }
        }).then((data) => {
            console.error("data", data);
            if(data.data.message === "공지사항 수정 완료"){
                alert("게시글이 수정되었습니다.");
                router.push('/notice/list');
            }
        }).catch((error: any) => {
            if(error.response.data.message === "만료된 토큰"){
                axiosBoard.put("/notice", {
                    boardId: boardId,
                    title: noticeTitle.current?.value,
                    content: noticeContent.current?.value,
                    writerName: notice.writerName,
                    writerProfileImg: notice.writerProfileImg,
                    fileURL: noticeFile.current?.value,
                    boardType: notice.boardType,
                    randomDanceId: 1,
                },{
                    headers:{
                        refreshToken: refreshToken,
                    }
                }).then((data) => {
                    if(data.data.message === "토큰 재발급 완료"){
                        setAccessToken(data.data.data.accessToken);
                        setRefreshToken(data.data.data.refreshToken);
                    }
                }).then(() => {
                    axiosBoard.put("/notice", {
                        boardId: boardId,
                        title: noticeTitle.current?.value,
                        content: noticeContent.current?.value,
                        writerName: notice.writerName,
                        writerProfileImg: notice.writerProfileImg,
                        fileURL: noticeFile.current?.value,
                        boardType: notice.boardType,
                        randomDanceId: 1,
                    },{
                        headers:{
                            Authorization: `Bearer ${accessToken}`,
                        }
                    }).then((data) => {
                        if(data.data.message === "공지사항 수정 완료"){
                            alert("게시글이 수정되었습니다.");
                            router.push('/notice/list');
                        }
                    })
                }).catch((data) => {
                    if(data.response.status === 401){
                        alert("장시간 이용하지 않아 자동 로그아웃 되었습니다.");
                        setNickaname("");
                        router.push("/login");
                        return;
                    }
                    if(data.response.status === 500){
                        alert("시스템 에러, 관리자에게 문의하세요.");
                        return;
                    }
                })
            }
        }).catch((error) => {
            console.error(error);
        })
    }

    useEffect(() => {
        axiosBoard.get(`/notice/${boardId}`, {
            params:{
                boardId: boardId,
            },
            headers:{
                Authorization: `Bearer ${accessToken}`
            }
        }).then((data) => {
            if(data.data.message === "공지사항 게시글 조회 완료"){
                noticeTitle.current.value = data.data.data.title;
                noticeContent.current.value = data.data.data.content;
                noticeFile.current.value = data.data.data.fileURL;
                setNotice(data.data.data);
            }
        }).catch((error: any) => {
            console.error("error", error);
            if(error.response.data.message === "만료된 토큰"){
                axiosBoard.get(`/notice/${boardId}`, {
                    params:{
                        boardId: boardId,
                    },
                    headers:{
                        refreshToken: refreshToken,
                    }
                }).then((data) => {
                    if(data.data.message === "토큰 재발급 완료"){
                        setAccessToken(data.data.data.accessToken);
                        setRefreshToken(data.data.data.refreshToken);
                    }
                }).then(() => {
                    axiosBoard.get(`/notice/${boardId}`, {
                        params:{
                            boardId: boardId,
                        },
                        headers:{
                            Authorization: `Bearer ${accessToken}`
                        }
                    }).then((data) => {
                        if(data.data.message === "공지사항 게시글 조회 완료"){
                            noticeTitle.current.value = data.data.data.title;
                            noticeContent.current.value = data.data.data.content;
                            noticeFile.current.value = data.data.data.fileURL;
                            setNotice(data.data.data);
                        }
                    })
                }).catch((data) => {
                    if(data.response.status === 401){
                        alert("장시간 이용하지 않아 자동 로그아웃 되었습니다.");
                        setNickaname("");
                        router.push("/login");
                        return;
                    }

                    if(data.response.status === 500){
                        alert("시스템 에러, 관리자에게 문의하세요.");
                        return;
                    }
                })
            }
        })
    }, [])
    return(
        <>
            <Header/>
            <MainBanner/>
            <SubNav linkNo="1"/>
            <div className="create-wrap">
                <div className="create-title">
                    <span>게시글</span>
                    <div className="flex-wrap">
                        <h3>글 작성</h3>
                        <div className="horizontal-line"></div>
                    </div>
                </div>
                <div className="create-content">
                    <form action="">
                        <table>
                            <tbody>
                            <tr>
                                <td>제목</td>
                                <td><input type="text" placeholder="제목을 입력해주세요." className="input-title" ref={noticeTitle}/></td>
                            </tr>
                            <tr>
                                <td>내용</td>
                                <td><textarea className="input-content" placeholder="내용을 입력해주세요." ref={noticeContent}></textarea></td>
                            </tr>
                            <tr>
                                <td>첨부파일</td>
                                <td><input type="file" accept="image/*" id="file-upload" ref={noticeFile}/></td>
                            </tr>
                            <tr>
                                <td></td>
                                <td>
                                    <div className="create-button-wrap">
                                        <ul>
                                            <li><button>취소하기</button></li>
                                            <li><button onClick={editNotice}>작성하기</button></li>
                                        </ul>
                                    </div>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </form>
                </div>
            </div>
            <Footer/>
        </>
    )
}

export default NoticeCreate;