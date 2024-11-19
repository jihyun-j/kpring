import { List } from "@mui/material";
import MemberListItem from "./MemberListItem";
import React from "react";
import { Member } from "../../types/layout";
import Profile from "../Profile/Profile";
import useModal from "../common/modal/hooks/useModal";
import ModalComponent from "../common/modal/Modal";

interface MemberListProps {
  memberList: Member[];
}

// TODO : 오른쪽 사이드바 멤버 리스트
const MemberList: React.FC<MemberListProps> = ({ memberList }) => {
  const { isOpen, openModal, closeModal } = useModal();

  return (
    <>
      <List
        sx={{
          overflow: "auto",
          "&::-webkit-scrollbar": {
            display: "none",
          },
          "-ms-overflow-style": "none",
          "scrollbar-width": "none",
        }}>
        {memberList.map((member, index) => (
          <MemberListItem
            member={member}
            key={index}
            handleProfileOpen={openModal}
          />
        ))}
      </List>
    </>
  );
};

export default MemberList;
