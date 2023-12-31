import { useEffect, useState } from "react";
import Header from "components/Header";
import MainBanner from "components/MainBanner";
import Footer from "components/Footer";
import SubNav from "components/subNav";
import { axiosBoard, axiosUser } from "apis/axios";

import Image from "next/image";
import { useRouter } from "next/router";
import Link from "next/link";
import CommentDefaultImage from "/public/images/comment-default-img.svg";

import { accessTokenState, refreshTokenState, idState, roleState, boardIdState, nicknameState } from "states/states";
import { useRecoilState } from "recoil";

const DetailNotice = () => {
    const router = useRouter();
    const boardId = router.query.no;
    const [article, setArticle] = useState<any>();
    const [accessToken, setAccessToken] = useRecoilState(accessTokenState);
    const [refreshToken, setRefreshToken] = useRecoilState(refreshTokenState);
    const [boardIdStat, setBoardIdStat] = useRecoilState(boardIdState); 
    const [id, setId] = useRecoilState(idState);
    const [role, setRole] = useRecoilState(roleState);
    const [nickname, setNickname] = useRecoilState(nicknameState);
    const createdDate = new Date(article?.createdAt);

    const formattedCreatedDate = createdDate.toLocaleString("ko-KR", {
        year: "numeric",
        month: "long",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });


    const deleteArticle = async () => {
        try{
            await axiosBoard.delete(`/notice/${boardId}`, {
                params:{
                    boardId: boardIdStat,
                },
                headers:{
                    Authorization: `Bearer ${accessToken}`,
                }
            }).then((data) => {
                if(data.data.message === "공지사항 삭제 완료"){
                    alert("해당 게시글이 삭제되었습니다.");
                    router.push('/notice/list');
                }
            }).catch((error: any) => {
                if(error.response.data.message === "만료된 토큰"){
                    axiosBoard.delete(`/notice/${boardId}`, {
                        params:{
                            boardId: boardIdStat,
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
                        axiosBoard.delete(`/notice/${boardId}`, {
                            params:{
                                boardId: boardIdStat,
                            },
                            headers:{
                                Authorization: `Bearer ${accessToken}`,
                            }
                        }).then((data) => {
                            console.log(data);
                            if(data.data.message === "공지사항 삭제 완료"){
                                alert("해당 게시글이 삭제되었습니다.");
                                router.push('/notice/list');
                            }
                        })
                    }).catch((data) => {
                        if(data.response.status === 401){
                            alert("장시간 이용하지 않아 자동 로그아웃 되었습니다.");
                            setNickname("");
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
        }catch(e){
            alert("글 삭제 실패, 관리자에게 문의하세요.");
        }
    };

	useEffect(() => {
		axiosBoard.get(`/notice/${boardIdStat}`).then((data) => {
			if (data.data.message === "공지사항 게시글 조회 완료") {
				setArticle(data.data.data);
			}
		})
	}, [])
	return (
		<>
			<Header></Header>
			<MainBanner></MainBanner>
			<SubNav linkNo="1"></SubNav>
			<div className="detail-article-wrap">
				<div className="detail-title">
					<span>공지사항</span>
					<div className="flex-wrap">
						<h3>글 상세보기</h3>
						<div className="vertical-line"></div>
					</div>
				</div>
				<div className="detail-content">
					<div className="list-wrap">
						<button><Link href="/notice/list">목록보기</Link></button>
					</div>
					<div className="detail-main-title">
						<span>공지사항</span>
						<h4>{article?.title}</h4>
                        <p>{formattedCreatedDate}</p>
					</div>
					<div className="detail-main-content">
						<p>{article?.content}</p>
					</div>
					<div className="button-wrap">
						{
							role === "ROLE_ADMIN" ?
								<>
									<button onClick={deleteArticle}>삭제하기</button>
									<button onClick={() => router.push('/notice/edit/' + article.boardId)}>수정하기</button>
								</>
								:
								<></>
						}
					</div>
				</div>
			</div>
			<Footer></Footer>
		</>
	)
}

export default DetailNotice;