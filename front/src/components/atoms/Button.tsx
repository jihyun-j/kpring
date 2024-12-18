type ButtonProps<
  E = React.MouseEvent<HTMLButtonElement> | React.FormEvent<HTMLButtonElement>
> = {
  children: React.ReactNode;
  icon?: React.ReactNode;
  onClick: (e: E) => void;
  color: string;
  disabled?: boolean;
};

// TODO: 스타일 변경
const Button: React.FC<ButtonProps> = ({
  children,
  icon,
  onClick,
  color,
  disabled = false,
}) => {
  return (
    <button
      className={`${color} px-3 py-1.5`}
      onClick={onClick}
      disabled={disabled}>
      {icon}
      {children}
    </button>
  );
};

export default Button;
